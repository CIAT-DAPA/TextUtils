package org.ciat.model;


/**@author c00kiemon5ter
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * 100% ################################################## |
 */
public class FileProgressBar {
    private StringBuilder progress;

    private long lenght;
    private int exp;
    private int dimensionality;
    private int total;
    private long done;
    private int lineNumber;
    /**
     * initialize progress bar properties.
     */
    public FileProgressBar(long fileLenght) {
    	this.lenght=fileLenght;
        init();
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done an int representing the work done so far
     * @param total an int representing the total work
     */
    private void update(int done, int total) {
        char[] workchars = {'|', '/', '-', '\\'};
        String format = "\r%3d%% %s %c";
        total= total<1?1:total;
        
        int percent = (++done * 100) / total;
        int extrachars = (percent / 2) - this.progress.length();

        while (extrachars-- > 0) {
            progress.append('#');
        }

        System.out.printf(format, percent, progress,
         workchars[done % workchars.length]);

        if (done == total) {
            System.out.flush();
            System.out.println();
            init();
        }
    }
    
    public void update(int lineLenght){
		/* show progress */
		done += lineLenght;
		if (++lineNumber % dimensionality == 0) {
			this.update(Math.toIntExact(done / dimensionality), total);
		}
		/* */
    }

    private void init() {
        this.progress = new StringBuilder(60);
		this.exp = (int) Math.ceil((lenght + "").length()) + 1;
		this.dimensionality = (int) Math.pow(2, exp);
		this.total = Math.toIntExact(lenght / dimensionality);
		this.done = lenght;
		this.lineNumber = 0;
		System.out.println("Processing " + lenght / 1024 + "KBs, updating progress each " + dimensionality + "KBs");
    }
}