/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package usbgroup37;

import java.util.Set;

/**
 *
 * @author ogone
 */
public interface UsbDetector {
     Set<UsbDrive> scanDevices();
}

