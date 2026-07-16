import { useCallback, useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setVoiceStatus } from '../redux/slices/voiceSlice';

const useTextToSpeech = () => {
  const dispatch = useDispatch();
  const synthRef = useRef(window.speechSynthesis);
  const { isMuted, language, pitch, rate, volume } = useSelector((state) => state.settings);
  const voicesRef = useRef([]);

  useEffect(() => {
    const loadVoices = () => {
      voicesRef.current = synthRef.current.getVoices();
    };
    
    loadVoices();
    if (synthRef.current.onvoiceschanged !== undefined) {
      synthRef.current.onvoiceschanged = loadVoices;
    }
  }, []);

  const speak = useCallback((text) => {
    if (isMuted || !text || !synthRef.current) {
      dispatch(setVoiceStatus('completed'));
      setTimeout(() => dispatch(setVoiceStatus('idle')), 2000);
      return;
    }

    // Cancel any ongoing speech before starting a new one
    synthRef.current.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    
    // Find best voice for language
    const voice = voicesRef.current.find(v => v.lang.startsWith(language.split('-')[0])) || voicesRef.current[0];
    if (voice) utterance.voice = voice;

    utterance.pitch = pitch;
    utterance.rate = rate;
    utterance.volume = volume;

    utterance.onstart = () => {
      dispatch(setVoiceStatus('speaking'));
    };

    utterance.onend = () => {
      dispatch(setVoiceStatus('completed'));
      setTimeout(() => dispatch(setVoiceStatus('idle')), 1500); // Wait a bit then idle
    };

    utterance.onerror = (e) => {
      console.error("Speech Synthesis Error:", e);
      dispatch(setVoiceStatus('idle'));
    };

    synthRef.current.speak(utterance);
  }, [isMuted, language, pitch, rate, volume, dispatch]);

  const stop = useCallback(() => {
    if (synthRef.current) {
      synthRef.current.cancel();
      dispatch(setVoiceStatus('idle'));
    }
  }, [dispatch]);

  return { speak, stop };
};

export default useTextToSpeech;
