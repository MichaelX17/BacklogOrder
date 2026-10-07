const { defaults } = require('jest-config');

module.exports = {
  preset: 'jest-expo',
  setupFiles: ['<rootDir>/jest.setup.js'],
  testMatch: [...defaults.testMatch],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/app/**',
    '!src/**/index.{ts,tsx}',
  ],
  coverageDirectory: '<rootDir>/coverage',
};
