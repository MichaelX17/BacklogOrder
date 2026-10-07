const globals = require('globals');
const expoConfig = require('eslint-config-expo/flat');

module.exports = [
  ...expoConfig,
  {
    ignores: ['dist/', 'coverage/', 'node_modules/'],
    settings: {
      'import/resolver': {
        typescript: {
          project: './tsconfig.json',
        },
      },
    },
  },
  {
    files: [
      'jest.config.js',
      'jest.setup.js',
      '**/__tests__/**/*.{ts,tsx}',
      '**/*.{test,spec}.{ts,tsx}',
    ],
    languageOptions: {
      globals: {
        ...globals.jest,
      },
    },
  },
];
