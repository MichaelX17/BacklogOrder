import { render, screen } from '@testing-library/react-native';

import HomeScreen from '@/app';

describe('HomeScreen', () => {
  it('renders the BacklogOrder title', async () => {
    await render(<HomeScreen />);

    expect(screen.getByText('BacklogOrder')).toBeTruthy();
    expect(screen.getByText('Hello')).toBeTruthy();
  });
});
