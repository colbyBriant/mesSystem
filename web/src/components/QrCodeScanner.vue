<template>
    <div class="qr-scanner">
      <video ref="video" class="scanner-video"></video>
      <div class="scanner-overlay">
        <div class="scanner-line"></div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  import { BrowserMultiFormatReader } from '@zxing/library'
  
  const emit = defineEmits(['result'])
  const video = ref(null)
  let codeReader = null
  
  const startScanning = async () => {
    try {
      codeReader = new BrowserMultiFormatReader()
      await codeReader.decodeFromVideoDevice(
        undefined,
        video.value,
        (result) => {
          if (result) {
            emit('result', result.text)
          }
        }
      )
    } catch (err) {
      console.error('扫码初始化失败:', err)
    }
  }
  
  onMounted(() => {
    startScanning()
  })
  
  onUnmounted(() => {
    if (codeReader) {
      codeReader.reset()
    }
  })
  </script>
  
  <style scoped>
  .qr-scanner {
    position: relative;
    width: 300px;
    height: 300px;
  }
  
  .scanner-video {
    width: 100%;
    height: 100%;
  }
  
  .scanner-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 2px solid #42b983;
  }
  
  .scanner-line {
    position: absolute;
    width: 100%;
    height: 2px;
    background: #42b983;
    animation: scan 2s linear infinite;
  }
  
  @keyframes scan {
    0% { top: 0; }
    100% { top: 100%; }
  }
  </style>