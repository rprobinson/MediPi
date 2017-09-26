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
package org.medipi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableMap;
import org.apache.commons.io.IOUtils;
import org.medipi.logging.MediPiLogger;

/**
 * Class to watch a the time server output file directory for changes to files.
 *
 * When a change to the timesync.txt file is detected, MediPi class time sync
 * status is updated for all elements to act upon and a message is given to the
 * lower banner alert
 */
public class TimeServerWatcher extends Thread {

    private static final String MEDIPITIMESYNCSERVERRESPONSESTRING = "medipi.timesyncserver.responsestring";
    private static final String ALERTBANNERMESSAGE = "Readings can't be taken until clock syncs...";
    private static final String CLASSKEY = "timeserver";
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private boolean trace = false;
    private final MediPi medipi;
    private final Path dir;
    private String timeSyncSuccessString = "";
    private AlertBanner alertBanner = AlertBanner.getInstance();

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    TimeServerWatcher(Path d, MediPi m) throws Exception {
        dir = d;
        medipi = m;
        // returned phrase to search for in the time server reponse
        String response = medipi.getProperties().getProperty(MEDIPITIMESYNCSERVERRESPONSESTRING);
        if (response == null || response.trim().length() == 0) {
            throw new Exception("Time sync server response string not configured");
        }
        timeSyncSuccessString = response;

        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        register();

        // enable trace after initial registration
        this.trace = true;

        testFileContents(dir.resolve("timesync.txt"));
        start();
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register() throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
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

        System.out.println("TimeServerWatcher run at: " + Instant.now());
        boolean running = true;
        while (running) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
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
                if (child.getFileName().toString().equals("timesync.txt")) {
                    String error = null;
                    if ((error = testFileContents(child)) != null) {
                        MediPiMessageBox.getInstance().makeErrorMessage(error, null);
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Can't access the Time server directory", null);
                    break;
                }
            }
        }
    }

    private String testFileContents(Path child) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(child.toString());
            String everything = IOUtils.toString(inputStream);
            if (everything.toLowerCase().contains(timeSyncSuccessString)) {
                medipi.timeSync.set(true);
                alertBanner.removeAlert(CLASSKEY);
                MediPiLogger.getInstance().log(TimeServerWatcher.class.getName(), "Time set from NTP server");
            } else {
                medipi.timeSync.set(false);
                alertBanner.addAlert(CLASSKEY, ALERTBANNERMESSAGE);
                MediPiLogger.getInstance().log(TimeServerWatcher.class.getName(), "Time not set yet from NTP server");
            }
        } catch (FileNotFoundException ex) {
            medipi.timeSync.set(false);
            alertBanner.addAlert(CLASSKEY, ALERTBANNERMESSAGE);
            MediPiLogger.getInstance().log(TimeServerWatcher.class.getName(), "Time not set from NTP server, file not accessible");
        } catch (IOException ex) {
            return "Can't access the Time server directory";
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    return "Can't access the Time server directory";
                }
            }
        }
        return null;
    }

}
