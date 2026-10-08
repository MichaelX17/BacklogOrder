import { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

import { HUD_THEME, rgba } from '@/theme/hudTheme';
import { useGamesStore } from '@/stores/gamesStore';
import { useListsStore } from '@/stores/listsStore';
import { exportAll, exportList, shareData } from '@/services/export';
import { importData } from '@/services/import';

export default function SettingsScreen() {
  const [isExporting, setIsExporting] = useState(false);
  const [isImporting, setIsImporting] = useState(false);

  const lists = useListsStore((state) => state.lists);
  const isLoaded = useListsStore((state) => state.isLoaded);

  async function handleExportAll() {
    try {
      setIsExporting(true);
      const data = await exportAll();
      await shareData(data, 'backlogorder-all');
    } catch (error) {
      console.error('Export error:', error);
      alert('Failed to export data');
    } finally {
      setIsExporting(false);
    }
  }

  async function handleExportSingleList(listId: string, listName: string) {
    try {
      setIsExporting(true);
      const data = await exportList(listId);
      await shareData(data, `backlogorder-${listName.toLowerCase().replace(/\s+/g, '-')}`);
    } catch (error) {
      console.error('Export error:', error);
      alert('Failed to export list');
    } finally {
      setIsExporting(false);
    }
  }

  async function handleImport() {
    try {
      setIsImporting(true);
      await importData();
      await useListsStore.getState().loadLists();
      await useGamesStore.getState().loadAllEntries();
      alert('Import completed successfully');
    } catch (error) {
      console.error('Import error:', error);
      alert(error instanceof Error ? error.message : 'Failed to import data');
    } finally {
      setIsImporting(false);
    }
  }

  if (!isLoaded) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={HUD_THEME.primary} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Settings</Text>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Export</Text>
        <Pressable
          style={[styles.button, isExporting && styles.buttonDisabled]}
          onPress={handleExportAll}
          disabled={isExporting}
        >
          {isExporting ? (
            <ActivityIndicator size="small" color={HUD_THEME.text} />
          ) : (
            <Text style={styles.buttonText}>Export All Lists</Text>
          )}
        </Pressable>

        {lists.map((list) => (
          <Pressable
            key={list.id}
            style={[styles.button, styles.secondaryButton, isExporting && styles.buttonDisabled]}
            onPress={() => handleExportSingleList(list.id, list.name)}
            disabled={isExporting}
          >
            <Text style={styles.buttonTextSecondary}>Export “{list.name}”</Text>
          </Pressable>
        ))}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Import</Text>
        <Pressable
          style={[styles.button, isImporting && styles.buttonDisabled]}
          onPress={handleImport}
          disabled={isImporting}
        >
          {isImporting ? (
            <ActivityIndicator size="small" color={HUD_THEME.text} />
          ) : (
            <Text style={styles.buttonText}>Import from File</Text>
          )}
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: HUD_THEME.bgFrom,
    padding: 16,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: HUD_THEME.bgFrom,
  },
  title: {
    fontSize: 24,
    fontWeight: '800',
    color: HUD_THEME.text,
    marginBottom: 24,
  },
  section: {
    backgroundColor: rgba(HUD_THEME.secondary, 0.05),
    borderRadius: 14,
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.18),
    padding: 16,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: HUD_THEME.text,
    marginBottom: 12,
    letterSpacing: 1,
  },
  button: {
    backgroundColor: HUD_THEME.primary,
    borderRadius: 10,
    padding: 12,
    alignItems: 'center',
    marginBottom: 8,
    shadowColor: HUD_THEME.primary,
    shadowOpacity: 0.3,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 0 },
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  buttonText: {
    color: '#120912',
    fontSize: 15,
    fontWeight: '800',
  },
  secondaryButton: {
    backgroundColor: rgba(HUD_THEME.secondary, 0.1),
    borderWidth: 1,
    borderColor: rgba(HUD_THEME.secondary, 0.2),
  },
  buttonTextSecondary: {
    color: HUD_THEME.secondary,
    fontSize: 15,
    fontWeight: '700',
  },
});
