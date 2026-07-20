const { spawn } = require('child_process');
const fs = require('fs');

console.log("Starting cloudflared tunnel...");
const child = spawn('npx', ['--yes', 'cloudflared', 'tunnel', '--url', 'http://localhost:5000'], { shell: true });

child.stderr.on('data', (data) => {
    const output = data.toString();
    console.log(output);
    const match = output.match(/https:\/\/[a-zA-Z0-9-]+\.trycloudflare\.com/);
    if (match) {
        console.log("FOUND URL: " + match[0]);
        fs.writeFileSync('tunnel_url.txt', match[0]);
    }
});

child.stdout.on('data', (data) => {
    console.log(data.toString());
});
