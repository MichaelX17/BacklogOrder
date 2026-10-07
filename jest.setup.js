jest.mock(
  'expo-secure-store',
  () => ({
    getItemAsync: jest.fn(async () => null),
    setItemAsync: jest.fn(async () => undefined),
    deleteItemAsync: jest.fn(async () => undefined),
  }),
  { virtual: true },
);

jest.mock(
  'expo-router',
  () => ({
    router: {
      push: jest.fn(),
      replace: jest.fn(),
      back: jest.fn(),
    },
    useRouter: () => ({
      push: jest.fn(),
      replace: jest.fn(),
      back: jest.fn(),
    }),
    useLocalSearchParams: () => ({}),
    usePathname: () => '/',
    useSegments: () => [],
    useRootNavigationState: () => null,
    Link: () => null,
    Redirect: () => null,
    Stack: () => null,
  }),
  { virtual: true },
);
