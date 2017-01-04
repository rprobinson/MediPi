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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.medipi.MediPiMessageBox;

/**
 * Class to watch the .scheduler file to be informed of any changes
 *
 * When a the .scheduler file is changed the schedule in Scheduler is refreshed
 */
public class ScheduleWatcher extends Thread {

    private WatchService watchService;
    private String fileName;
    private Path dir;
    private Scheduler sched;
    private WatchKey watchKey;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    ScheduleWatcher(Path d, String f, Scheduler s) {
        dir = d;
        fileName = f;
        sched = s;
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.watchKey = dir.register(watchService, ENTRY_MODIFY);
            start();
        } catch (IOException ex) {
            Logger.getLogger(ScheduleWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Override
    public void run() {
        for (;;) {
            // wait for key to be signalled
            try {
                watchKey = watchService.take();
            } catch (InterruptedException x) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Scheduler - failed to instantiate key", x);
                return;
            }

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                //we only register "ENTRY_MODIFY" so the context is always a Path.
                Path changed = (Path) event.context();
                System.out.println(changed);
                if (changed.endsWith(fileName)) {
                    sched.refreshSchedule();
                }
            }
            // reset the key
            boolean valid = watchKey.reset();
            if (!valid) {
                System.out.println("Key has been unregistered");
            }

        }

    }
}
