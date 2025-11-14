package ar.com.hipotecario.canal.tas.modulos.auditoria.ticket;

import java.util.List;

/**
 * This class encapsulates the whole interface for the Ticket to PDF generation engine.
 * 
 * The class is expected to be a singleton (meaning: there will be only **ONE** instance of the
 * class). To get that instance, you should use the static method getInstance.
 * 
 * The first step is to configure the class (passing in some paths and configuration options).
 *
 * IMPORTANT: this configuration step must be performed **ONLY** once in the program, before any use.
 * 
 * After it is configured, you can get TicketFile objects (which encapsulate the idea of a ticket file
 * that can be updated with fragments).
 * 
 * You can also get Converter objects, that perform the actual conversion from TicketFile to a PDF file.
 * 
 * @author jcampanello
 *
 */
public class Ticket2PDF {

    private static Ticket2PDF instance;

    private String ticketRepoPath;
    private String pdfRepoPath;
    private String html2PDFConverter;
    
    private Ticket2PDF() {

        // nothing to do
    }

    /**
     * Construct the instance of the class
     */
    static {
        instance = new Ticket2PDF();
    }

//    /** return the instance */
    public static Ticket2PDF getInstance() {
        return instance;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // getters/setters
    //
    
    public String getTicketRepoPath() {
        return ticketRepoPath;
    }

    public void setTicketRepoPath(String ticketRepoPath) {
        this.ticketRepoPath = ticketRepoPath;
    }

    public String getPdfRepoPath() {
        return pdfRepoPath;
    }

    public void setPdfRepoPath(String pdfRepoPath) {
        this.pdfRepoPath = pdfRepoPath;
    }

    public String getHtml2PDFConverter() {
        return html2PDFConverter;
    }

    public void setHtml2PDFConverter(String html2pdfConverter) {
        html2PDFConverter = html2pdfConverter;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // TicketFile
    //

//    /** get a new ticket file */
    public TicketFile getTicketFile(Integer terminalId) {

        return new TicketFile(terminalId);
    }

//    /** get an existing ticket file */
    public TicketFile getTicketFile(String ticketId) {

        return new TicketFile(ticketId);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Converter
    //

    public Converter getConverter() {
        
        return new Converter();
    }

    public Converter getConverter(TicketFile ticket) {
        
        return new Converter(ticket);
    }

    public Converter getConverter(TicketFile ticket, String title) {
        
        return new Converter(ticket, title);
    }

    public Converter getConverter(TicketFile ticket, String title, List<String> subtitles) {
        
        return new Converter(ticket, title, subtitles);
    }
}
