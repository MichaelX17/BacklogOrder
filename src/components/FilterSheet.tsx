import { useState } from 'react';
import { Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { hudThemes, withAlpha } from '@/theme/hudTheme';
import { GAME_STATUSES, type GameStatus } from '@/types';
import { DEFAULT_FILTERS, type FilterState } from '@/utils/filters';

interface FilterSheetProps {
  visible: boolean;
  filters: FilterState;
  onClose: () => void;
  onApply: (filters: FilterState) => void;
}

export default function FilterSheet({ visible, filters, onClose, onApply }: FilterSheetProps) {
  const [draft, setDraft] = useState<FilterState>(filters);

  function updateNumericField(
    key: 'playtimeMin' | 'playtimeMax' | 'ratingMin' | 'ratingMax' | 'scoreMin' | 'scoreMax',
    rawValue: string,
  ) {
    const trimmed = rawValue.trim();
    if (trimmed === '') {
      setDraft((current) => ({ ...current, [key]: null }));
      return;
    }

    const parsed = Number(rawValue);
    setDraft((current) => ({
      ...current,
      [key]: Number.isFinite(parsed) ? parsed : null,
    }));
  }

  function toggleStatus(status: GameStatus) {
    setDraft((current) => ({
      ...current,
      statuses: current.statuses.includes(status)
        ? current.statuses.filter((candidate) => candidate !== status)
        : [...current.statuses, status],
    }));
  }

  function handleApply() {
    onApply(draft);
    onClose();
  }

  function handleClear() {
    setDraft(DEFAULT_FILTERS);
    onApply(DEFAULT_FILTERS);
    onClose();
  }

  return (
    <Modal transparent visible={visible} animationType="slide" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose}>
        <Pressable style={styles.sheet} onPress={() => undefined}>
          <View style={styles.handle} />
          <Text style={styles.title}>Filter games</Text>

          <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.content}>
            <View style={styles.fieldGroup}>
              <Text style={styles.label}>Name</Text>
              <TextInput
                style={styles.input}
                value={draft.name}
                onChangeText={(text) => setDraft((current) => ({ ...current, name: text }))}
                placeholder="Search by title"
                placeholderTextColor={hudThemes.violet.colors.muted}
              />
            </View>

            <View style={styles.row}>
              <View style={styles.fieldGroupFlex}>
                <Text style={styles.label}>Playtime min</Text>
                <TextInput
                  style={styles.input}
                  value={draft.playtimeMin === null ? '' : String(draft.playtimeMin)}
                  onChangeText={(text) => updateNumericField('playtimeMin', text)}
                  placeholder="0"
                  keyboardType="numeric"
                />
              </View>

              <View style={styles.fieldGroupFlex}>
                <Text style={styles.label}>Playtime max</Text>
                <TextInput
                  style={styles.input}
                  value={draft.playtimeMax === null ? '' : String(draft.playtimeMax)}
                  onChangeText={(text) => updateNumericField('playtimeMax', text)}
                  placeholder="∞"
                  keyboardType="numeric"
                />
              </View>
            </View>

            <View style={styles.row}>
              <View style={styles.fieldGroupFlex}>
                <Text style={styles.label}>Rating min</Text>
                <TextInput
                  style={styles.input}
                  value={draft.ratingMin === null ? '' : String(draft.ratingMin)}
                  onChangeText={(text) => updateNumericField('ratingMin', text)}
                  placeholder="0"
                  keyboardType="numeric"
                />
              </View>

              <View style={styles.fieldGroupFlex}>
                <Text style={styles.label}>Rating max</Text>
                <TextInput
                  style={styles.input}
                  value={draft.ratingMax === null ? '' : String(draft.ratingMax)}
                  onChangeText={(text) => updateNumericField('ratingMax', text)}
                  placeholder="100"
                  keyboardType="numeric"
                />
              </View>
            </View>

            <View style={styles.row}>
              <View style={styles.fieldGroupFlex}>
                <Text style={styles.label}>Score min</Text>
                <TextInput
                  style={styles.input}
                  value={draft.scoreMin === null ? '' : String(draft.scoreMin)}
                  onChangeText={(text) => updateNumericField('scoreMin', text)}
                  placeholder="0"
                  keyboardType="numeric"
                />
              </View>

              <View style={styles.fieldGroupFlex}>
                <Text style={styles.label}>Score max</Text>
                <TextInput
                  style={styles.input}
                  value={draft.scoreMax === null ? '' : String(draft.scoreMax)}
                  onChangeText={(text) => updateNumericField('scoreMax', text)}
                  placeholder="10"
                  keyboardType="numeric"
                />
              </View>
            </View>

            <View style={styles.fieldGroup}>
              <Text style={styles.label}>Status</Text>
              <View style={styles.chipRow}>
                {GAME_STATUSES.map((status) => {
                  const selected = draft.statuses.includes(status);
                  return (
                    <Pressable
                      key={status}
                      style={[styles.chip, selected && styles.chipSelected]}
                      onPress={() => toggleStatus(status)}
                    >
                      <Text style={[styles.chipText, selected && styles.chipTextSelected]}>
                        {status}
                      </Text>
                    </Pressable>
                  );
                })}
              </View>
            </View>
          </ScrollView>

          <View style={styles.actions}>
            <Pressable style={[styles.secondaryButton]} onPress={handleClear}>
              <Text style={styles.secondaryButtonText}>Clear</Text>
            </Pressable>
            <Pressable style={[styles.primaryButton]} onPress={handleApply}>
              <Text style={styles.primaryButtonText}>Apply</Text>
            </Pressable>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'withAlpha(0, 0, 0, 0.55)',
  },
  sheet: {
    backgroundColor: hudThemes.violet.colors.bgFrom,
    borderTopLeftRadius: 22,
    borderTopRightRadius: 22,
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.2),
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 24,
    maxHeight: '85%',
  },
  handle: {
    alignSelf: 'center',
    width: 52,
    height: 5,
    borderRadius: 999,
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.5),
    marginBottom: 12,
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: hudThemes.violet.colors.text,
    marginBottom: 12,
    letterSpacing: 1,
  },
  content: {
    paddingBottom: 12,
  },
  fieldGroup: {
    marginBottom: 16,
  },
  fieldGroupFlex: {
    flex: 1,
  },
  label: {
    fontSize: 13,
    fontWeight: '700',
    color: hudThemes.violet.colors.muted,
    marginBottom: 8,
    letterSpacing: 1,
  },
  input: {
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.06),
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.22),
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 15,
    color: hudThemes.violet.colors.text,
  },
  row: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chip: {
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.05),
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.2),
    marginBottom: 8,
  },
  chipSelected: {
    backgroundColor: hudThemes.violet.colors.primary,
    borderColor: hudThemes.violet.colors.primary,
  },
  chipText: {
    fontSize: 12,
    fontWeight: '700',
    color: hudThemes.violet.colors.text,
    letterSpacing: 0.8,
  },
  chipTextSelected: {
    color: '#120912',
  },
  actions: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 12,
  },
  primaryButton: {
    flex: 1,
    backgroundColor: hudThemes.violet.colors.primary,
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
    shadowColor: hudThemes.violet.colors.primary,
    shadowOpacity: 0.35,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 0 },
  },
  primaryButtonText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#120912',
  },
  secondaryButton: {
    flex: 1,
    backgroundColor: withAlpha(hudThemes.violet.colors.secondary, 0.09),
    borderRadius: 10,
    borderWidth: 1,
    borderColor: withAlpha(hudThemes.violet.colors.secondary, 0.2),
    paddingVertical: 12,
    alignItems: 'center',
  },
  secondaryButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: hudThemes.violet.colors.secondary,
  },
});
