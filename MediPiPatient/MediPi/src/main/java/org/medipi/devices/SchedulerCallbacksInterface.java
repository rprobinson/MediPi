/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.devices;

/**
 *
 * @author riro
 */
public interface SchedulerCallbacksInterface {
    // Called directly after the scheduler reloads its schedule from the disk
    public void ScheduleRefreshed();
    // Called directly after the Scheduler recognises that the current schedule has expired and a new schedule is due
    public void ScheduleExpired();
}
