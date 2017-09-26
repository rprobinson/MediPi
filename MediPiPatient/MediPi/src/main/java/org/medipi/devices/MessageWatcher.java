/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.devices;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.medipi.AlertBanner;
import org.medipi.MediPi;

/**
 * Class to watch a the incoming messages directory for changes to files.
 *
 * When a new file is detected, the message List is updated in Messenger and an
 * alert badge is superimposed onto the Dashboard Tile. The tile is also
 * coloured Red and a message is inserted into the lower alert banner
 */
public class MessageWatcher extends Thread {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private boolean trace = false;
    private final MessageReceiver messageReceiver;
    private final MediPi medipi;
    private final Path dir;
    private AlertBanner alertBanner = AlertBanner.getInstance();

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    MessageWatcher(Path d, MessageReceiver mr, MediPi medipi) throws IOException {
        dir = d;
        messageReceiver = mr;
        this.medipi = medipi;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        register();

        // enable trace after initial registration
        this.trace = true;

        start();
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register() throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
        }
        keys.put(key, dir);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Override
    public void run() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                messageReceiver.callFailure("Messenger - failed to instantiate key", x);
                return;
            }

            Path path = keys.get(key);
            if (path == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = path.resolve(name);
                if (child.getFileName().toString().endsWith(".txt")) {
                    ObservableList<Message> items = FXCollections.observableArrayList();
                    File list[] = new File(path.toString()).listFiles();
                    Arrays.sort(list, (File f1, File f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()));
                    for (File f : list) {
                        Message m;
                        try {
                            m = new Message(f.getName());
                        } catch (Exception e) {
                            continue;
                        }
                        items.add(m);
                    }
                    Platform.runLater(() -> {
                        messageReceiver.setMessageList(items);
                        if (event.kind().name().equals("ENTRY_CREATE")) {
                            messageReceiver.newMessageReceived(new File(child.toString()));
                        }
                    });

                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    messageReceiver.callFailure("Messenger - Directories are inaccessible", null);
                    break;
                }
            }
        }
    }

}
