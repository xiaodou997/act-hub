import { defineConfig, globalIgnores } from 'eslint/config'
import globals from 'globals'
import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

export default defineConfig([
  // ====== 忽略构建产物 ======
  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  // ====== 全局配置：所有文件 ======
  {
    name: 'app/files-to-lint',
    files: ['**/*.{js,mjs,jsx,vue}'], // 支持 JS 和 Vue
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node, // 如果用到 Node 全局变量（如 process）
      },
    },
  },

  // ====== JavaScript 推荐规则 ======
  js.configs.recommended,

  // ====== Vue 3 推荐规则（核心）======
  ...pluginVue.configs['flat/essential'], // 或 'flat/recommended' 更强规则

  // ====== Vue 文件专属配置（关键！）======

  // ===== JS 基础配置 =====
  {
    files: ['**/*.{js,mjs,jsx}'],
    languageOptions: {
      parser: 'espree', // 默认 JS 用 espree
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },
    rules: {
      ...js.configs.recommended.rules,
    },
  },

  // ===== TS 文件配置 =====
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      parser: '@typescript-eslint/parser',
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      // TS 推荐规则
      ...require('@typescript-eslint/eslint-plugin').configs.recommended.rules,
    },
  },

  // ===== Vue 文件配置 =====
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: pluginVue.parser, // vue-eslint-parser
      parserOptions: {
        // 这里指定内部 parser：如果 lang=ts 就走 ts，否则走 espree
        parser: {
          ts: '@typescript-eslint/parser',
          js: 'espree',
        },
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      ...pluginVue.configs['flat/essential'].rules,
      // Vue 推荐 TS 用 type-based 声明
      'vue/define-props-declaration': ['warn', 'type-based'],
      'vue/define-emits-declaration': ['warn', 'type-based'],
    },
  },

  // ====== Prettier 跳过格式化规则（避免冲突）======
  skipFormatting,
])
