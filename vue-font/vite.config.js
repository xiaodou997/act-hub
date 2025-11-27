import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import removeConsole from 'vite-plugin-remove-console'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const isProduction = mode === 'production'

  const plugins = [vue(), vueDevTools()]
  if (isProduction) {
    plugins.push(removeConsole({ includes: ['log', 'debug'] }))
  }

  return {
    base: '/', // 如果你的项目部署在这个路径
    plugins,
    server: {
      port: 9911, // 指定端口号
      strictPort: true, // 若端口被占用则直接退出，避免自动切换
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    worker: {
      format: 'es', // 使用ES模块
      plugins: () => [], // 可以添加worker专用插件
    },
    build: {
      minify: 'esbuild',
      esbuild: {
        // 生产环境压缩配置
        minifyIdentifiers: true,
        minifySyntax: true,
        minifyWhitespace: true,
        legalComments: 'none', // 移除所有注释
        target: 'es2020',
      },
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            element: ['element-plus'],
            // xlsx: ['xlsx'],

            // axios: ['axios'],
            // echarts: ['echarts'],
            // lodash: ['lodash'],
          },
          entryFileNames: 'assets/[name].[hash].js',
          chunkFileNames: 'assets/[name].[hash].js', // 添加哈希避免缓存问题
          assetFileNames: 'assets/[name].[hash].[ext]',
        },
      },

      // 其他优化
      sourcemap: isProduction ? false : 'inline', // 生产环境关闭 sourcemap
      chunkSizeWarningLimit: 2000,
    },
  }
})
