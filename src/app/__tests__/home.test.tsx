import { render, screen } from '@testing-library/react-native';

import HomeScreen from '@/app';

describe('HomeScreen', () => {
  it('renders the HUD home shell with the design headline', async () => {
    await render(<HomeScreen />);

    expect(screen.getByText('Up Next')).toBeTruthy();
    expect(screen.getByText('Offline ready')).toBeTruthy();
  });

  it('keeps the list creation affordance visible', async () => {
    await render(<HomeScreen />);

    expect(screen.getByText('Create')).toBeTruthy();
  });
});
