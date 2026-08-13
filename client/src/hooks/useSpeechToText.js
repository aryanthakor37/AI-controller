import { useEffect, useRef, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setVoiceStatus, setVoiceError } from '../redux/slices/voiceSlice';
import { setInterimTranscript, setFinalTranscript } from '../redux/slices/speechSlice';
import { addMessage } from '../redux/slices/conversationSlice';
import { fetchHistory } from '../redux/slices/commandSlice';
import api from '../services/api';
import useTextToSpeech from './useTextToSpeech';
import socketService from '../services/socketService';

const useSpeechToText = () => {
  const dispatch = useDispatch();
  const recognitionRef = useRef(null);
  const { status } = useSelector((state) => state.voice);
  const { language } = useSelector((state) => state.settings);
  const { activeDevices } = useSelector((state) => state.device);
  const { speak } = useTextToSpeech();

  useEffect(() => {
    // Initialize Web Speech API
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    
    if (!SpeechRecognition) {
      dispatch(setVoiceError('Browser does not support Speech Recognition.'));
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = language;

    recognition.onstart = () => {
      dispatch(setVoiceStatus('listening'));
    };

    recognition.onresult = (event) => {
      let interim = '';
      let final = '';

      for (let i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) {
          final += event.results[i][0].transcript;
        } else {
          interim += event.results[i][0].transcript;
        }
      }

      if (interim) dispatch(setInterimTranscript(interim));
      if (final) {
        dispatch(setFinalTranscript(final));
        processVoiceCommand(final);
      }
    };

    recognition.onerror = (event) => {
      console.error('Speech recognition error:', event.error);
      if (event.error !== 'no-speech') {
        dispatch(setVoiceError(event.error));
        dispatch(setVoiceStatus('error'));
      } else {
        dispatch(setVoiceStatus('idle'));
      }
    };

    recognition.onend = () => {
      // If we stopped listening but haven't started processing yet, revert to idle
      if (storeRef.current?.voice?.status === 'listening') {
         dispatch(setVoiceStatus('idle'));
      }
    };

    recognitionRef.current = recognition;

    return () => {
      if (recognitionRef.current) recognitionRef.current.abort();
    };
  }, [dispatch, language]);

  // Use a ref to check latest status and activeDevices in callbacks
  const storeRef = useRef(null);
  useEffect(() => {
    storeRef.current = { voice: { status }, device: { activeDevices } };
  }, [status, activeDevices]);

  const processVoiceCommand = async (transcript) => {
    if (!transcript.trim()) return;

    dispatch(setVoiceStatus('processing'));
    dispatch(addMessage({ role: 'user', text: transcript }));

    try {
      // Send transcript to Backend AI Engine (Phase 4 Gemini integration)
      const response = await api.post('/voice/respond', { transcript });
      
      const intentData = response.data?.data || {};
      const { spokenResponse, intent } = intentData;
      
      dispatch(addMessage({ role: 'ai', text: spokenResponse, intent }));

      // Forward intent command via WebSocket to active device
      if (intent && intent !== 'UNKNOWN_COMMAND' && intent !== 'GENERAL_CHAT') {
        const activeDevs = storeRef.current?.device?.activeDevices || activeDevices;
        const targetDeviceId = (activeDevs && activeDevs.length > 0) ? activeDevs[0].socketId : 'all';
        socketService.sendCommand(targetDeviceId, intentData);
        dispatch(fetchHistory());
      }
      
      // Speak the response via TTS
      speak(spokenResponse);

    } catch (error) {
      console.error('Voice Processing API Error:', error);
      dispatch(setVoiceError('Failed to reach AI Engine.'));
      dispatch(setVoiceStatus('error'));
    }
  };

  const startListening = useCallback(() => {
    if (recognitionRef.current && status !== 'listening') {
      try {
        recognitionRef.current.start();
      } catch (err) {
        console.warn('Recognition already started');
      }
    }
  }, [status]);

  const stopListening = useCallback(() => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      dispatch(setVoiceStatus('idle'));
    }
  }, [dispatch]);

  return {
    startListening,
    stopListening,
    isListening: status === 'listening',
    isProcessing: status === 'processing'
  };
};

export default useSpeechToText;
