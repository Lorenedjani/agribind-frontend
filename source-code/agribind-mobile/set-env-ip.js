const os = require('os');
const fs = require('fs');
const path = require('path');

function getLocalIpAddress() {
    const interfaces = os.networkInterfaces();
    let bestIp = null;

    for (const name of Object.keys(interfaces)) {
        // Skip virtual interfaces (VMware, WSL, VirtualBox, etc.)
        const isVirtual = name.toLowerCase().includes('vmware') || 
                          name.toLowerCase().includes('virtual') || 
                          name.toLowerCase().includes('vbox') ||
                          name.toLowerCase().includes('wsl') ||
                          name.toLowerCase().includes('vethernet') ||
                          name.toLowerCase().includes('loopback');

        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                if (!isVirtual) {
                    // Return the first real physical interface (e.g., "Wi-Fi" or "Ethernet")
                    return iface.address;
                }
                // Save virtual IP as a fallback just in case
                if (bestIp === null) {
                    bestIp = iface.address;
                }
            }
        }
    }
    return bestIp || '127.0.0.1';
}

const envPath = path.join(__dirname, '.env');
const localIp = getLocalIpAddress();

console.log(`[set-env-ip] Detected local IP: ${localIp}`);

if (fs.existsSync(envPath)) {
    let envContent = fs.readFileSync(envPath, 'utf8');
    
    const updatedContent = envContent.replace(
        /(EXPO_PUBLIC_API_BASE_URL=http:\/\/)([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)(:[0-9]+)/g,
        `$1${localIp}:8081`
    );

    if (envContent !== updatedContent) {
        fs.writeFileSync(envPath, updatedContent, 'utf8');
        console.log(`[set-env-ip] Updated .env file with new target API IP: ${localIp}:8081`);
    } else {
        console.log(`[set-env-ip] .env file is already up to date (${localIp}:8081).`);
    }
} else {
    console.log(`[set-env-ip] No .env file found. Creating one...`);
    const defaultPort = 8081;
    fs.writeFileSync(envPath, `EXPO_PUBLIC_API_BASE_URL=http://${localIp}:${defaultPort}\n`, 'utf8');
}
