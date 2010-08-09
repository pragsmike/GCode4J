GCode4J
=============

This is the java code I use to send gcode to the arduino gcode interpreter that runs my CNC router.

This code might be useful as an example if you are writing a gcode sender.
If you just want ready-made software to send gcode, you probably want to use something else.
Check out the [rstep project](http://groups.google.com/group/rstep).

The code here is quirky and undocumented.  It only works with the quirky and undocumented fork I made from the originally excellent reprap gcode code last year.
The best thing I can say about it is that it would be easy to improve it.

Run org.gorb.gcode.ui.Main.  It will put up a window that resembles an application.  The port menu lists the serial ports it found.  Choose the one that the arduino is on.  If you pick 'simulator' it will act like there is an interpreter on the other end that always succeeds, so you can see what codes it would send.

This code uses the serial library from Processing.  That in turn uses the gnu serial library.  It would be better if this code just used the gnu library directly and did away with the Processing hack.
