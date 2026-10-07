import { initializeDatabase } from '@/db/client';
import { gamesRepo, listGamesRepo, listsRepo } from '@/db/repositories';

export function seedDatabase(): void {
  initializeDatabase();

  const backlog = listsRepo.create('Backlog');
  const playing = listsRepo.create('Playing Now');

  const witcher = gamesRepo.create({
    name: 'The Witcher 3: Wild Hunt',
    metacritic: 92,
    playtime: 50,
    genres: ['RPG'],
    platforms: ['PC', 'PlayStation 4', 'Xbox One'],
  });

  const hades = gamesRepo.create({
    name: 'Hades',
    metacritic: 93,
    playtime: 20,
    franchise: 'Hades',
    franchiseOrder: 1,
  });

  const hadesTwo = gamesRepo.create({
    name: 'Hades II',
    metacritic: 95,
    playtime: 30,
    franchise: 'Hades',
    franchiseOrder: 2,
  });

  const mystery = gamesRepo.create({
    name: 'Mystery Title',
    rating: 4.2,
    playtime: 15,
  });

  const unfinished = gamesRepo.create({
    name: 'No Data Game',
  });

  listGamesRepo.addGameToList(backlog.id, witcher.id, 'Backlog');
  listGamesRepo.addGameToList(backlog.id, hades.id, 'Backlog');
  listGamesRepo.addGameToList(backlog.id, mystery.id, 'Backlog');
  listGamesRepo.addGameToList(backlog.id, unfinished.id, 'Backlog');
  listGamesRepo.addGameToList(playing.id, hadesTwo.id, 'Playing');
}
