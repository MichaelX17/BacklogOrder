import { render, screen } from '@testing-library/react-native';

import HomeScreen from '@/app';

describe('HomeScreen', () => {
  it('renders the lists section', async () => {
    await render(<HomeScreen />);

    expect(screen.getByText('Your lists')).toBeTruthy();
    expect(screen.getByText('Create')).toBeTruthy();
  });

  it('shows the empty state when there are no lists', async () => {
    await render(<HomeScreen />);

    expect(screen.getByText('No lists yet')).toBeTruthy();
  });
});
