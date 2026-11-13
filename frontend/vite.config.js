import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 后端统一跑在 8082（8080 被本机代理占用）
      '/api': 'http://127.0.0.1:8082'
    }
  },
  build: {
    // 直接输出到 Spring Boot 静态资源目录，构建后单 jar 即可部署
    outDir: '../src/main/resources/static',
    emptyOutDir: true
  }
})
