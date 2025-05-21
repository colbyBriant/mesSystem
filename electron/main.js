const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const fs = require('fs');

let mainWindow;
let serverProcess;

function startServer() {
    return new Promise((resolve, reject) => {
        try {
            const appPath = app.isPackaged 
                ? process.resourcesPath
                : path.join(__dirname, '..');

            // 确保日志目录存在
            const logsDir = path.join(appPath, 'logs');
            if (!fs.existsSync(logsDir)) {
                fs.mkdirSync(logsDir, { recursive: true });
            }

            const startScript = path.join(appPath, 'start-mes-server.bat');
            console.log('Starting server with script:', startScript);

            // 使用 cmd /c 运行批处理文件
            serverProcess = spawn('cmd.exe', ['/c', startScript], {
                windowsHide: true,
                cwd: appPath
            });

            // 30秒后自动解析（即使没有看到启动消息）
            setTimeout(() => {
                console.log('Server start timeout, continuing anyway...');
                resolve();
            }, 10000);
        } catch (error) {
            console.error('Error in startServer:', error);
            reject(error);
        }
    });
}

function stopServer() {
    const appPath = app.isPackaged 
        ? process.resourcesPath
        : path.join(__dirname, '..');
        
    const stopScript = path.join(appPath, 'stop-mes-server.bat');
    
    return new Promise((resolve) => {
        spawn('cmd.exe', ['/c', stopScript], {
            windowsHide: true,
            cwd: appPath
        }).on('close', () => {
            serverProcess = null;
            resolve();
        });
    });
}

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1280,
        height: 800,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false
        }
    });

    mainWindow.loadFile(path.join(__dirname, '../web/dist/index.html'));
}

app.whenReady().then(() => {
    try {
        // 同时启动服务器和窗口
        startServer().catch(error => {
            console.error('Server start error:', error);
        });
        
        // 立即创建窗口
        createWindow();
    } catch (error) {
        console.error('Failed to start application:', error);
        app.quit();
    }
});

app.on('window-all-closed', async () => {
    await stopServer();
    if (process.platform !== 'darwin') {
        app.quit();
    }
});