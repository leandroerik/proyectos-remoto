package ar.com.hipotecario.canal.tas.modulos.auditoria.ticket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;


public class TicketFile {

    private String ticketId;
    


    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // constructors (protected)
    //

    protected TicketFile(Integer terminalId) {

        // we need a ticketId
        this.ticketId = createTicketId(terminalId);
    }

    protected TicketFile(String ticketId) {
        
        // keep the ticket id
        this.ticketId = ticketId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // getters
    //

    public String getTicketId() {

        return this.ticketId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // ticket manipulation API
    //

    public void ticketCreate() throws IOException, Ticket2PDFException {

        // we make sure the file exists
        File file = ensureFile();

        // the file must be empty, else it's an error
        if (file.length() != 0) {
            throw new Ticket2PDFException("Cannot create TicketFile " + getTicketId() +
                                          "because it already exists and is not empty");
        }

        // start with an HTML basic document
        append(file, "<html>");
    }

    public void ticketComplete() throws IOException, Ticket2PDFException {

        // we make sure the file exists
        File file = ensureFile();

        // the file must not be empty, else it's an error
        if (file.length() == 0) {
            throw new Ticket2PDFException("Cannot complete TicketFile " + getTicketId() +
                    "because it already exists but is empty");
        }

        // end with the HTML basic document
        append(file, "</html>");
    }

    public void ticketAppend(String fragment) throws IOException, Ticket2PDFException {

        // we make sure the file exists
        File file = ensureFile();

        // the file must not be empty, else it's an error
        if (file.length() == 0) {
            throw new Ticket2PDFException("Cannot append to TicketFile " + getTicketId() +
                    "because it already exists but is empty");
        }
        fragment = fragment.replace("&amp;#209;", "&#209;");
        // end with the HTML basic document
        appendTicket(file, fragment);
    }

    public void ticketDestroy() {

        try {
        
            // we make sure the file exists
            File file = ensureFile();

            // if the file exists, destroy it
            if (file.exists()) {
           	
                file.delete();
            }
        } catch (IOException ioe) {
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // internal file handling
    //

    private void append(File file, String fragment) throws IOException {

        // prepare to write to file
        FileWriter writer = new FileWriter(file, true);

        // append the fragment
        writer.write(fragment);
        writer.write("\n");
    
        // close the file
        writer.close();
    }
    
    private void appendTicket(File file, String ticket) throws IOException {

        // prepare to write to file
        FileWriter writer = new FileWriter(file, true);

        // append the fragment
        writer.write(ticket);
    
        // close the file
        writer.close();
    }
    
    private String createTicketId(Integer terminalId) {
        
        // get the current date
        Calendar curDate = new GregorianCalendar();

        // format the file name
        String ticketId = String.format("%1$08x_%2$tY%2$tm%2$td_%2$tH%2$tM%2$tS.%2$tL_%3$8x.html",
                                        terminalId, curDate, new Random().nextInt());
        
        return ticketId;
    }

    protected String getAbsoluteFileName() {

        // get the global API object
        Ticket2PDF t2p = Ticket2PDF.getInstance();
        
        return(t2p.getTicketRepoPath() + File.separator + getTicketId() + ".html");
    }
    
    private File ensureFile() throws IOException {
        
        // build the complete file name
        File file = new File(getAbsoluteFileName());
        
        // if file does not exists => create 
        if ( ! file.exists()) {
            file.createNewFile();
        }
        
        return file;
    }
}
