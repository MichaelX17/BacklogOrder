import { render, screen } from '@testing-library/react-native';

import StatusBadge from '../StatusBadge';
import type { GameStatus } from '@/types';

describe('StatusBadge', () => {
  const statuses: GameStatus[] = ['Backlog', 'Playing', 'Completed', 'Dropped'];

  it.each(statuses)('renders the %s status', async (status) => {
    await render(<StatusBadge status={status} />);

    expect(screen.getByText(status)).toBeTruthy();
  });
});
