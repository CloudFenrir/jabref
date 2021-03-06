package org.jabref.gui.collab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jabref.gui.util.BackgroundTask;
import org.jabref.gui.util.TaskExecutor;
import org.jabref.logic.util.io.FileUtil;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.util.FileUpdateListener;
import org.jabref.model.util.FileUpdateMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseChangeMonitor implements FileUpdateListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseChangeMonitor.class);

    private final BibDatabaseContext database;
    private final FileUpdateMonitor fileMonitor;
    private final List<DatabaseChangeListener> listeners;
    private Path referenceFile;
    private TaskExecutor taskExecutor;

    public DatabaseChangeMonitor(BibDatabaseContext database, FileUpdateMonitor fileMonitor, TaskExecutor taskExecutor) {
        this.database = database;
        this.fileMonitor = fileMonitor;
        this.taskExecutor = taskExecutor;
        this.listeners = new ArrayList<>();

        this.database.getDatabasePath().ifPresent(path -> {
            try {
                fileMonitor.addListenerForFile(path, this);
                referenceFile = Files.createTempFile("jabref", ".bib");
                referenceFile.toFile().deleteOnExit();
                setAsReference(path);
            } catch (IOException e) {
                LOGGER.error("Error while trying to monitor " + path, e);
            }
        });
    }

    @Override
    public void fileUpdated() {
        // File on disk has changed, thus look for notable changes and notify listeners in case there are such changes
        ChangeScanner scanner = new ChangeScanner(database);
        BackgroundTask.wrap(scanner::scanForChanges)
                      .onSuccess(changes -> {
                          if (!changes.isEmpty()) {
                              listeners.forEach(listener -> listener.databaseChanged(changes));
                          }
                      })
                      .executeWith(taskExecutor);
    }

    public void addListener(DatabaseChangeListener listener) {
        listeners.add(listener);
    }

    public void unregister() {
        database.getDatabasePath().ifPresent(file -> fileMonitor.removeListener(file, this));
    }

    public void markExternalChangesAsResolved() {
        markAsSaved();
    }

    public void markAsSaved() {
        database.getDatabasePath().ifPresent(this::setAsReference);
    }

    private void setAsReference(Path file) {
        FileUtil.copyFile(file, referenceFile, true);
    }
}
