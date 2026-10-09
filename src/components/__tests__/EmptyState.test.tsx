import { render, screen } from '@testing-library/react-native';

import { EmptyState } from '../EmptyState';

describe('EmptyState', () => {
  it('renders title and message', async () => {
    await render(<EmptyState title="Nothing here" message="Add something to get started." />);

    expect(screen.getByText('Nothing here')).toBeTruthy();
    expect(screen.getByText('Add something to get started.')).toBeTruthy();
  });

  it('renders optional actions', async () => {
    await render(
      <EmptyState title="Empty" message="Try again.">
        <EmptyState title="Nested" message="x" />
      </EmptyState>,
    );

    expect(screen.getByText('Empty')).toBeTruthy();
    expect(screen.getByText('Nested')).toBeTruthy();
  });
});
