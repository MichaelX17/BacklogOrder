import { useEffect, useState } from 'react';
import { Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { mapRawgGame } from '@/services/rawg/mapper';
import type { RawgGame } from '@/services/rawg/types';
import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';
import type { GameList, GameStatus } from '@/types';
import { GAME_STATUSES } from '@/types';

interface AddToListModalProps {
  visible: boolean;
  game: RawgGame | null;
  onClose: () => void;
  onAdded: () => void;
}

export default function AddToListModal({ visible, game, onClose, onAdded }: AddToListModalProps) {
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      {visible && game !== null ? (
        <AddToListModalContent game={game} onClose={onClose} onAdded={onAdded} />
      ) : null}
    </Modal>
  );
}

interface AddToListModalContentProps {
  game: RawgGame;
  onClose: () => void;
  onAdded: () => void;
}

function AddToListModalContent({ game, onClose, onAdded }: AddToListModalContentProps) {
  const lists = useListsStore((state) => state.lists);
  const createList = useListsStore((state) => state.createList);
  const addGameToList = useGamesStore((state) => state.addGameToList);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);
  const [newListName, setNewListName] = useState('');
  const [status, setStatus] = useState<GameStatus>('Backlog');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    useListsStore.getState().loadLists();
  }, []);

  function handleAdd() {
    const mapped = mapRawgGame(game);

    let listId = selectedListId;
    if (listId === null) {
      const trimmed = newListName.trim();
      if (trimmed === '') {
        setError('Pick a list or enter a new list name.');
        return;
      }
      const created = createList(trimmed);
      listId = created.id;
    }

    addGameToList(listId, mapped, status);
    onAdded();
  }

  return (
    <Pressable style={styles.backdrop} onPress={onClose}>
      <Pressable style={styles.sheet} onPress={() => undefined}>
        <Text style={styles.title} numberOfLines={1}>
          {game.name}
        </Text>

        <Text style={styles.label}>List</Text>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.chipRow}
        >
          {lists.map((list: GameList) => (
            <Pressable
              key={list.id}
              style={[styles.chip, selectedListId === list.id && styles.chipSelected]}
              onPress={() => {
                setSelectedListId(list.id);
                setNewListName('');
                setError(null);
              }}
            >
              <Text
                style={[styles.chipText, selectedListId === list.id && styles.chipTextSelected]}
              >
                {list.name}
              </Text>
            </Pressable>
          ))}
        </ScrollView>
        <TextInput
          style={styles.input}
          value={newListName}
          onChangeText={(text) => {
            setNewListName(text);
            if (text.trim() !== '') {
              setSelectedListId(null);
            }
            setError(null);
          }}
          placeholder="Or create a new list"
          placeholderTextColor="#999999"
        />

        <Text style={styles.label}>Initial state</Text>
        <View style={styles.chipRow}>
          {GAME_STATUSES.map((candidate) => (
            <Pressable
              key={candidate}
              style={[styles.chip, status === candidate && styles.chipSelected]}
              onPress={() => setStatus(candidate)}
            >
              <Text style={[styles.chipText, status === candidate && styles.chipTextSelected]}>
                {candidate}
              </Text>
            </Pressable>
          ))}
        </View>

        {error !== null ? <Text style={styles.error}>{error}</Text> : null}

        <View style={styles.actions}>
          <Pressable
            style={({ pressed }) => [styles.cancelButton, pressed && styles.buttonPressed]}
            onPress={onClose}
          >
            <Text style={styles.cancelButtonText}>Cancel</Text>
          </Pressable>
          <Pressable
            style={({ pressed }) => [styles.addButton, pressed && styles.buttonPressed]}
            onPress={handleAdd}
          >
            <Text style={styles.addButtonText}>Add to list</Text>
          </Pressable>
        </View>
      </Pressable>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: '#ffffff',
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
    padding: 24,
    paddingBottom: 32,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111111',
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111111',
    marginBottom: 8,
    marginTop: 8,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 8,
  },
  chip: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 8,
  },
  chipSelected: {
    backgroundColor: '#111111',
    borderColor: '#111111',
  },
  chipText: {
    fontSize: 14,
    color: '#111111',
  },
  chipTextSelected: {
    color: '#ffffff',
  },
  input: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    color: '#111111',
    backgroundColor: '#ffffff',
    marginBottom: 8,
  },
  error: {
    marginTop: 8,
    fontSize: 14,
    color: '#d32f2f',
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 24,
    gap: 12,
  },
  cancelButton: {
    borderWidth: 1,
    borderColor: '#cccccc',
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  cancelButtonText: {
    fontSize: 15,
    color: '#111111',
  },
  addButton: {
    backgroundColor: '#111111',
    borderRadius: 8,
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  addButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#ffffff',
  },
  buttonPressed: {
    opacity: 0.7,
  },
});
