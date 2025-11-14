package ar.com.hipotecario.canal.tas.modulos.auditoria.ticket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class Converter {

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // class variables
    //

    static Map<String, Integer> mediaClasses = null;

    static {
        // create the map
        mediaClasses = new HashMap<String, Integer>();

        // populate the map
        mediaClasses.put("_default_", 1);
        mediaClasses.put("narrower",  1);
        mediaClasses.put("narrow",    2);
        mediaClasses.put("wide",      3);
        mediaClasses.put("wider",     4);
    };

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // instance variables
    //
    private TicketFile      ticket;
    private String          title;
    private List<String>  subtitles;

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // constructors (protected)
    //

    protected Converter() {

    }
    
    protected Converter(TicketFile ticket) {
        
        this.ticket = ticket;
    }

    protected Converter(TicketFile ticket, String title) {

        this.ticket     = ticket;
        this.title      = title;
    }

    protected Converter(TicketFile ticket, String title, List<String> subtitles) {

        this.ticket     = ticket;
        this.title      = title;
        this.subtitles  = subtitles;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // getters/setters
    //

    public TicketFile getTicket() {
        return ticket;
    }

    public void setTicket(TicketFile ticket) {
        this.ticket = ticket;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(List<String> subtitles) {
        this.subtitles = subtitles;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    // conversion
    //

    private String getAbsoluteFileName(String pdfFileName) {

        // get the global API object
        Ticket2PDF t2p = Ticket2PDF.getInstance();

        return(t2p.getPdfRepoPath() + File.separator + pdfFileName);
    }

    /**
     * Convert the input ticket document into a PDF
     * @param pdfFileName the filename (local to the PDF repository) where the pdf must be generated
     * @throws Ticket2PDFException if the input document cannot be parsed, modified (to be HTML) or converted (to PDF)
     * @throws FileNotFoundException if the input file does not exists
     * @throws IOException if the output file cannot be created
     */
    public void convert(String pdfFileName) throws Ticket2PDFException, FileNotFoundException, IOException {

        // get input and output file names 
        String iFileName = this.ticket != null ? this.ticket.getAbsoluteFileName() : null;
    	
        String oFileName = getAbsoluteFileName(pdfFileName);

        // make sure source and destination file exists
        File iFile = new File(iFileName);
        File oFile = new File(oFileName);
        if ( ! iFile.exists()) {
            throw new FileNotFoundException("Cannot convert TicketFile " +
                                            (this.ticket != null ? this.ticket.getTicketId() : "<unknown>") +
                                            "because it does not exists");
        }
        if ( ! oFile.exists()) {
            oFile.createNewFile();
        }

        // parse the document
        Document doc = parseDocument(iFileName);
        if (doc == null) {
            throw new Ticket2PDFException("Cannot parse TicketFile " +
                                          (this.ticket != null ? this.ticket.getTicketId() : "<unknown>"));
        }

        // convert the document to be HTML
        doc = convertDocument(doc);
        if (doc == null) {
            throw new Ticket2PDFException("Cannot convert TicketFile " +
                                          (this.ticket != null ? this.ticket.getTicketId() : "<unknown>") +
                                          " to HTML.");
        }

        // write the document
        writeDocument(doc, iFileName);
        
        // create the pdf
        createPDF(iFileName, oFileName);
    }

    private void createPDF(String iFileName, String oFileName) throws Ticket2PDFException {

        // get the global API object and the command to execute
        Ticket2PDF t2p = Ticket2PDF.getInstance();
        String command = t2p.getHtml2PDFConverter();

        // make sure output files do no exit
        fileDelete(oFileName);
        fileDelete(oFileName + "_tmp");
        
        // prepare a process
        ProcessBuilder pb = new ProcessBuilder(command, "--quiet", iFileName, oFileName + "_tmp");

        // start the process
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException ioe) {
            throw new Ticket2PDFException("Failed to convert TicketFile " +
                                          (this.ticket != null ? this.ticket.getTicketId() : "<unknown>") +
                                          " to PDF (unable to start converter process with path [" + command + "]).",
                                          ioe);
        }

        // wait until it's finished
        int resultCode = -1;
        try {
            resultCode = p.waitFor();
        } catch (InterruptedException ie) {
            throw new Ticket2PDFException("Failed to convert TicketFile " +
                                          (this.ticket != null ? this.ticket.getTicketId() : "<unknown>") +
                                          " to PDF (unable to start converter process with path [" + command + "]).",
                                          ie);
        }

        // rename the pdf to the final name
        if (resultCode == 0) {

            // rename the file to the final name
            if ( ! fileRename(oFileName + "_tmp", oFileName)) {
                throw new Ticket2PDFException("Failed to convert TicketFile " +
                                              (this.ticket != null ? this.ticket.getTicketId() : "<unknown>") +
                                              " to PDF (unable to rename temporary file [" + oFileName + "_tmp" + "]" +
                                              "to the final name).");
                
            }
        } else {

            // remove the file (if present)
            fileDelete(oFileName + "_tmp");
            
            throw new Ticket2PDFException("Failed to convert TicketFile " +
                    (this.ticket != null ? this.ticket.getTicketId() : "<unknown>") +
                    " to PDF (converter error code " + Integer.valueOf(resultCode).toString() + ").");
        }
    }

    private boolean fileDelete(String src) {

        // delete the file (if it exists)
        File file = new File(src);
        try {
            file.delete();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean fileRename(String src, String dst) {

        // try to rename the file
        File file = new File(src);
        try {
            file.renameTo(new File(dst));
        } catch (Exception e) {
            return false;
        }

        return true;
    }
    
    /**
     * writes a document (which should be HTML) into the input Ticket file
     */
    private void writeDocument(Document doc, String iFileName) throws IOException {

        // convert the document to html
        String html = doc.html();
        
        // prepare to write to file
        OutputStreamWriter writer =  new OutputStreamWriter(new FileOutputStream(iFileName),Charset.forName("UTF-8").newEncoder());
        // FileWriter writer = new FileWriter(iFileName, false);

        // append the fragment
        writer.write(html);

        // close the file
        writer.close();
    }

    /**
     * Parses the ticket document as a DOM structure
     * @param iFile the file object representing the document
     * @return the parsed document
     * @throws IOException if the file does not exists or cannot be read, etc
     * @throws FileNotFoundException if the input file does not exists
     */
    private Document parseDocument(String iFileName) throws IOException {
    	
    	FileInputStream fis= new FileInputStream(iFileName);
    	
        // parse the document
        Document doc = Jsoup.parse(fis, null, ".", Parser.xmlParser());
        
        fis.close();

        return doc;
    }
    
    public void convertDocumentTicket(Document doc) {
    	doc = convertDocument(doc);
    	
    }

    /**
     * Converts a Ticket Document (propietary xml, similar to HTML) into HTML.
     * 
     * The process is as follows:
     * <ol>
     * <li>get the root document node that is the <em>body</em> of the document and clean all attributes</li>
     * <li>move the children of the remaining body elements to the first body and delete the emptied body elements</li>
     * <li>create a div object named <strong>ticket</strong> with class <em>ticket</em> and append it to the <em>newBody</em></li>
     * <li>get the list <strong>children</strong> as all existing elements of the body</li>
     * <li>process each entry in the list <strong>children</strong> as follows:</li>
     *     <ol>
     *         <li>remove the current child from the document body</li>
     *         <li>ignore the node if it is not an elements or text node</li>
     *         <li>if it's an element node, convert it with a specific method depending on it's tag name (div, graphic, barcode, etc)</li>
     *         <li>append the current node to the <strong>ticket</strong> div (this removes the node from the <em>body</em>)</li>
     *     </ol>
     * <li>put titles and subtitles in a div with class <em>titles</em> and that div into the <em>body</em></li>
     * <li>append the <strong>ticket</strong> node the <em>body</em></li>
     * <li>append the <em>title</em> and <em>meta charset</em> node to the <em>head</em> of the document</li>
     * <li>append the <strong>styles</strong> node the <em>head</em> of the document</li>
     * <li>return the converted document</li>
     * </ol>
     * @param doc the document to convert
     * @return the document modified
     */
    private Document convertDocument(Document doc) {
        
        // 1: get the root document node that is the <em>body</em> of the document and clean all attributes
        Element body = doc.body();
        for(Attribute attr : body.attributes()) {
            body.removeAttr(attr.getKey());
        }

        // 2: move the children of the remaining body elements to the first body and delete the emptied body elements
        Elements bodies = doc.getElementsByTag("body");
        int      bodyCount = bodies != null ? bodies.size() : 1;
        for(int i = 1; i < bodyCount; i++) {
            
            // get the unnecessary body element
            Element uBody = bodies.get(i);
            
            // append the children
            body.insertChildren(-1, uBody.childNodes());

            // remove the unnecessary body element
            uBody.remove();
        }

        // 3: create a div object named <strong>ticket</strong> with class <em>ticket</em>
        Element ticket = createElem(doc, "div", null, "class", "ticket");

        // 4: get the list <strong>bodies</strong> as all existing elements of the <em>body</em>
        Elements children = body.children();

        // 5: process each entry in the list <strong>children</strong> as follows:
        int childrenLen = children.size();
        for(int c = 0; c < childrenLen; c++) {

            // get the specific node
            Node node = children.get(c);

            // 5.1: remove the current child from the document body
            node.remove();
            
            // 5.2: ignore nodes that are not elements or text nodes
            // we are only interested in Elements and Text nodes
            if ( ! (node instanceof Element) && ! (node instanceof TextNode) ) {
                continue;
            }

            // 5.3: if it's an element node, convert it with a specific method depending on it's tag
            //      name (div, graphic, barcode, etc)
            if (node instanceof Element) {
                
                // cast as element
                Element elem = (Element) node;
                String tagName = elem.tagName();
                if (tagName.equalsIgnoreCase("div")) {
                    elem = convertNodeDiv(doc, elem);
                }
                if (tagName.equalsIgnoreCase("barcode")) {
                    elem = convertNodeBarcode(doc, elem);
                }
                if (tagName.equalsIgnoreCase("graphic")) {
                    elem = convertNodeGraphic(doc, elem);
                }
                if (tagName.equalsIgnoreCase("table")) {
                    elem = convertNodeTable(doc, elem);
                }
                if (tagName.equalsIgnoreCase("cut")) {
                    elem = convertNodeCut(doc);
                }
                if (tagName.equalsIgnoreCase("line")) {
                    elem = convertNodeLine(doc, elem);
                }

                // just in case, assign again to the node
                node = elem;
            }               

            // 5.4: append the current node to the <strong>ticket</strong> div
            if (node != null) {
                ticket.appendChild(node);
            }
        }

        // 6: put titles and subtitles in a div with class <em>titles</em> and that div into the <em>body</em>
        //
        // NOTE: we allways put a title, subtitles are optional
        //
        Element titles = createElem(doc, "div", null, "class", "titles");
        titles.appendChild(createElem(doc, "h1", this.title != null ? this.title : "Ticket de TAS"));
        if (this.subtitles != null) {

            // iterate the subtitles, creating an h2 for each subtitle
            for(String subtitle : this.subtitles) {
                titles.appendChild(createElem(doc, "h2", subtitle));
            }
        }
        body.appendChild(titles);

        // 7: append the <strong>ticket</strong> node the <em>body</em>
        body.appendChild(ticket);

        // 8: append the <em>title</em> and <em>meta charset</em> node to the <em>head</em> of the document
        Element head = doc.head();
        if (head == null) {
            head = createElem(doc, "head");
            doc.prependChild(head);
        }
        head.appendChild(createElem(doc, "title", this.title != null ? this.title : "Ticket de TAS"));
        Element metaContentType = createElem(doc, "meta","");
        metaContentType.attr("http-equiv", "Content-Type");
        metaContentType.attr("content", "text/html;charset=" + doc.charset());
        head.appendChild(metaContentType);

        // 9: append the <strong>styles</strong> node the <em>head</em> of the document
        head.appendChild(createElem(doc, "style",
                "\n" +
                "/* ******* RESET HTML STYLES ******* */\n" +
                "html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre,\n" +
                "a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp,\n" +
                "small, strike, strong, sub, sup, tt, var, b, u, i, center,\n" +
                "dl, dt, dd, ol, ul, li, fieldset, form, label, legend,\n" +
                "table, caption, tbody, tfoot, thead, tr, th, td,\n" +
                "article, aside, canvas, details, embed,\n" +
                "figure, figcaption, footer, header, hgroup,\n" +
                "menu, nav, output, ruby, section, summary,\n" +
                "time, mark, audio, video {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    border: 0;\n" +
                "    font-size: 100%;\n" +
                "    font: inherit;\n" +
                "    vertical-align: baseline;\n" +
                "}\n" +
                "\n" +
                "/* HTML5 display-role reset for older browsers */\n" +
                "article, aside, details, figcaption, figure, footer, header, hgroup, menu, nav, section { display: block; }\n" +
                "body { line-height: 1; }\n" +
                "ol, ul { list-style: none; }\n" +
                "blockquote, q { quotes: none; }\n" +
                "blockquote:before, blockquote:after, q:before, q:after { content: ''; content: none; }\n" +
                "table { border-collapse: collapse; border-spacing: 0; }\n" +
                "\n" +
                "/* ******* Cyberbank Devices Ticket Styles ******* */\n" +
                "\n" +
                "div.titles { width: 100%; text-align: center; margin: 15px; }\n" +
                "div.titles h1 { text-align: center; margin-top: 20px; margin-bottom: 20px;}\n" +
                "div.titles h2 { text-align: left; }\n" +
                "\n" +
                "div.ticket { width: 65%; margin: 0 auto; margin-top: 30px; padding: 8px; font-family: \"Courier New\", monospace; font-size: 11px; border: solid 1px #000; }\n" +
                "\n" +
                "div.ticket img { margin: 15px; }\n" +
                "\n" +
                "div.ticket table.line { width: 100%; }\n" +
                "div.ticket table.line tr.filler_dotted { border-bottom: dotted 1px; }\n" +
                "div.ticket table.line tr.filler_dashed { border-bottom: dashed 1px; }\n" +
                "div.ticket table.line td.left { text-align: left; }\n" +
                "div.ticket table.line td.center { text-align: center; }\n" +
                "div.ticket table.line td.right { text-align: right; }\n" +
                "\n" +
                "div.ticket table.table { width: 100%; text-align: left; }\n" +
                "div.ticket table.table th, div.ticket table.table td { padding: 0 0.5em; }\n" +
                "div.ticket table.table tr.filler_dotted { border-bottom: dotted 1px; }\n" +
                "div.ticket table.table tr.filler_dashed { border-bottom: dashed 1px; }\n" +
                "div.ticket table.table tr.separator_both { border-bottom: #000 solid 1px; border-top: #000 solid 1px; }\n" +
                "div.ticket table.table tr.separator_pre { border-top: #000 solid 1px; }\n" +
                "div.ticket table.table tr.separator_post { border-bottom: #000 solid 1px; }\n" +
                "\n" +
                "div.ticket .variant-1h2w, div.ticket .variant-2h1w, div.ticket .variant-2h2w {\n" +
                "    -webkit-transform-origin: 0 0;\n" +
                "    -moz-transform-origin: 0 0;\n" +
                "    -ms-transform-origin: 0 0;\n" +
                "    -o-transform-origin: 0 0;\n" +
                "    transform-origin: 0 0;\n" +
                "}\n" +
                "div.ticket .variant-1h2w {\n" +
                "    -webkit-transform: scaleX(2);\n" +
                "    -moz-transform: scaleX(2);\n" +
                "    -ms-transform: scaleX(2);\n" +
                "    -o-transform: scaleX(2);\n" +
                "    transform: scaleX(2);\n" +
                "}\n" +
                "div.ticket .variant-2h1w {\n" +
                "    -webkit-transform: scaleY(2);\n" +
                "    -moz-transform: scaleY(2);\n" +
                "    -ms-transform: scaleY(2);\n" +
                "    -o-transform: scaleY(2);\n" +
                "    transform: scaleY(2);\n" +
                "}\n" +
                "div.ticket .variant-2h2w {\n" +
                "    -webkit-transform: scaleX(2) scaleY(2);\n" +
                "    -moz-transform: scaleX(2) scaleY(2);\n" +
                "    -ms-transform: scaleX(2) scaleY(2);\n" +
                "    -o-transform: scaleX(2) scaleY(2);\n" +
                "    transform: scaleX(2) scaleY(2);\n" +
                "}\n" +
                "\n" +
                "\n"));

        // 10: return the converted document
        return doc;
    }
    
    private Element convertNodeDiv(Document doc, Element div) {

        // convert "<div><span>=====....===</span></div>" to an "hr"
        Elements children = div.children();
        if (children == null || children.size() == 0) {

            // replace an empty div with a <br/>
            return createElem(doc, "br");
        }
        if (children.size() == 1 && "span".equals(children.get(0).tagName())) {

            // check if the span has only a text element and are all "="
            Element span = children.get(0);
            if (span.children() == null || span.children().size() == 0) {

                // get the text nodes
                List<TextNode> texts = span.textNodes();
                if (texts != null && texts.size() == 1) {

                    // get the text and see what it is
                    String text = texts.get(0).text();
                    if (text != null && "".equals(text.replaceAll("=", ""))) {
                        return createElem(doc, "hr");
                    }
                }
            }
        }
        
        // if style class => change the style and add a class
        String styleClass = hasFontVariant(div.attr("style"));
        if (styleClass != null) {

            // get the style
            String style = convertStyle(div.attr("style"));

            // modify the style and add the class
            div.attr("style", style);
            div.addClass(styleClass);
        }
        
        return div;
    }

    private Element convertNodeLine(Document doc, Element line) {

        // get the style (if any)
        String style        = convertStyle(line.attr("style"));
        String styleClass   = hasFontVariant(line.attr("style"));
                
        // get the fill (if any)
        String filler = getFillerClass(line);

        // get the left and right items
        Element  leftNode   = getFirstChild(line, "left");
        Element  centerNode = getFirstChild(line, "center");
        Element  rightNode  = getFirstChild(line, "right");

        // get the left and right text
        String left   = leftNode   != null ? leftNode.text()   : null;
        String center = centerNode != null ? centerNode.text() : null;
        String right  = rightNode  != null ? rightNode.text()  : null;

        // create a table and a single record
        Element table = createElem(doc, "table", null, "class", "line");
        Element tr = createElem(doc, "tr", null, style);
        if (styleClass != null) {
            tr.addClass(styleClass);
        }
        if (filler != null) {
            tr.addClass(filler);
        }
        table.appendChild(tr);

        // add the columns to the record
        if (left != null) {                 // add the left column
            tr.appendChild(createElem(doc, "td", left, "class", "left"));
        }
        if (center != null) {               // add the center column
            tr.appendChild(createElem(doc, "td", center, "class", "center"));
        }
        if (right != null) {                // add the right column
            tr.appendChild(createElem(doc, "td", right, "class", "right"));
        }

        return table;
    }

    private Element convertNodeBarcode(Document doc, Element barcode) {

        // get the code element of the barcode
        Element  code     = getFirstChild(barcode, "code");

        // the text we will put
        String text = null;
        
        // if we have a code => get the value and type and build the text
        if (code != null) {

            // get value and type
            String value = code.hasText() ? code.text() : null;
            String type  = code.attr("type");

            // build the text (if we at least have a value)
            if (value != null) {

                text = "Bar Code: " + value + (type != null ? " (Tipo " + type + ")" : "");
            }
        }

        // if we still have no text => try to get the alt text
        if (text == null) {

            // get the alt node (if any) and get the alt text
            Element  alt        = getFirstChild(barcode, "alt");

            // get the text
            text = alt != null && alt.hasText() ? alt.text() : null;
        }
        
        // if no text yet => put a generic
        if (text == null) {
            text = "MISSING BARCODE VALUE!";
        }

        // return a div with the text
        Element div = createElem(doc, "div");
        div.appendChild(createElem(doc, "span", text));

        return div;
    }

    /**
     * gets a Cyberbank Devices print <em>graphic</em> element and returns a new <em>img</em> node with the proper image
     * @param doc the document
     * @param elem the graphic element
     * @return the new img node
     */
    private Element convertNodeGraphic(Document doc, Element graphic) {

        // get the bitmap element of the graphic
        Element  bitmap     = getFirstChild(graphic, "bitmap");

        // get the alt node (if any) and get the alt text
        Element  alt        = getFirstChild(graphic, "alt");
        String   altText    = alt != null && alt.hasText() ? alt.text() : null;

        // if there is a bitmap element => get the file attribute and return the IMG
        if (bitmap != null) {

            // get the file attribute
            String fname = bitmap.attr("file");

            // if it's not a default image => skip it
            if (fname.indexOf('@') != -1) {
                fname = null;
            }

            // if we still have a name
            if (fname != null) {

                // make the fname be a jpg
                fname = fname + ".png";

                // create the img
                Element img = createElem(doc, "img", null, "src", fname);

                // set the alt text (if any)
                if (altText != null) {
                    img.attr("alt", altText);
                }
                
                // return the img
                return img;
            }
        }

        // if we don't have an alt text => put a default
        if (altText == null) {
            altText = "MISSING IMAGE!";
        }

        // return a div with the alt text
        Element div = createElem(doc, "div");
        div.appendChild(createElem(doc, "span", altText));

        return div;
    }

    /**
     * gets a Cyberbank Devices print <em>cut</em> element and returns a new <em>hr</em> node
     * @param doc the document
     * @param cut the cut node
     * @return the replacement hr node
     */
    private Element convertNodeCut(Document doc) {

        return createElem(doc, "hr");
    }

    /**
     * gets a Cyberbank Devices print <em>table</em> element and returns a new <em>table</em> node
     * prepared as pure HTML
     * @param doc the document
     * @param table the table node
     * @return the replacement table node
     */
    private Element convertNodeTable(Document doc, Element table) {

        // get all the media nodes from the table
        Elements mediaList = table.getElementsByTag("media");
        if (mediaList == null || mediaList.isEmpty()) {
            // it's a standard table => just return it
            return table;
        }
        
        // iterate the media list to look for the best media format
        Element bestMedia = null;
        for(Element media : mediaList) {
            
            // remove the media from the table (we don't want media's thead and tbody to interfere later)
            media.remove();
            
            // we use the first as default
            if (bestMedia == null) {
                bestMedia = media;
                continue;
            }

            // get the coefficients for best media (so far) and current media
            // NOTE: if no media class we use a default, if media class value is unknown, we use the default
            Integer bestMediaCoef = mediaClasses.get(bestMedia.hasAttr("media-class") ? bestMedia.attr("media-class") : "_default_");
            if (bestMediaCoef == null) {
                bestMediaCoef = mediaClasses.get("_default_");
            }
            Integer mediaCoef = mediaClasses.get(media.hasAttr("media-class") ? media.attr("media-class") : "_default_");
            if (mediaCoef == null) {
                mediaCoef = mediaClasses.get("_default_");
            }
            
            // choose based on coefficients
            if (bestMediaCoef < mediaCoef) {
                bestMedia = media;
            }
        }

        // calculate how many columns the table will have
        int colCount = -1;
        Elements mediaRows = bestMedia!=null ? bestMedia.getElementsByTag("row") :  null;
        if (mediaRows != null && mediaRows.size() > 0) {

            // iterate the rows, counting the number of defined columns
            for(Element row : mediaRows) {
                Elements columns = row.getElementsByTag("col");
                if (columns != null && columns.size() > colCount) {
                    colCount = columns.size();
                }
            }
        }
        
        // get the media head and the media body
        Element mediaHead = getFirstChild(bestMedia, "thead");
        Element mediaBody = getFirstChild(bestMedia, "tbody");
        Element mediaFoot = getFirstChild(bestMedia, "tfoot");
        
        // prepare the result table
        Element result = createElem(doc, "table", null, getStyle(table));
        result.addClass("table");

        // process the thead (if present)
        Element head    = getFirstChild(table, "thead");
        Element headRow = null;
        if (head != null) {

            // get the first head row
            headRow = getFirstChild(head, "row");
            
            // add the head node to the result table
            result.appendChild(emmitTableComponent(doc, head, mediaHead, null, colCount));
        }

        // process the tbody (if present)
        Element body = getFirstChild(table, "tbody");
        if (body != null) {
            
            // add the head node to the result table
            result.appendChild(emmitTableComponent(doc, body, mediaBody, headRow, colCount));
        }

        // process the tfoot (if present)
        Element foot    = getFirstChild(table, "tfoot");
        if (foot != null) {

            // add the head node to the result table
            result.appendChild(emmitTableComponent(doc, foot, mediaFoot, null, colCount));
        }

        return result;
    }

    /**
     * Receives a table part (<em>thead</em>, <em>tbody</em> or <em>tfoot</em>), a media definition and (optionally)
     * a thead row and creates the proper set of nodes in the <em>result</em> table. 
     * @param doc the document
     * @param tPart the table part (<em>thead</em>, <em>tbody</em> or <em>tfoot</em>)
     * @param mediaDef the media definition
     * @param headRow the optional thead row
     * @param colCount the total column count (used for <em>colspan</em> in single row columns)
     * @return the new table part
     */
    private Element emmitTableComponent(Document doc, Element tPart, Element mediaDef, Element headRow, int colCount) {

        // decide if it's a tfoot
        boolean isTFoot = "tfoot".equalsIgnoreCase(tPart.tagName());
        
        // create the new part and append to the result table
        Element newPart = createElem(doc, tPart.tagName(), null, getStyle(tPart));

        // get all the rows for the table part
        Elements rows = tPart.children();
        if (rows == null || rows.size() == 0) {
            return newPart;
        }

        // get the media def rows
        Elements mediaRows = mediaDef.children();
        if (mediaRows != null && mediaRows.size() == 0) {
            mediaRows = null;
        }
        
        // iterate the part's rows
        for(Element row : rows) {
            
            // if we have media rows => iterate them, else emit all the columns
            if (mediaRows != null) {

                // get the row columns
                Elements columns = row.children();
                
                // iterate the media rows
                for(Element mediaRow : mediaRows) {

                    // decide if we allow empty rows
                    boolean allowEmpty = mediaRow.hasAttr("allow-empty") ? "true".equalsIgnoreCase(mediaRow.attr("allow-empty")) : false;
                    
                    // create the new row
                    Element newRow = createElem(doc, "tr", null, combineStyles(row, mediaRow));

                    // get the fill (if any) and append it to the row
                    String filler = getFillerClass(row, mediaRow);
                    if (filler != null) {
                        newRow.addClass(filler);
                    }

                    // get the separator (if any) and append it to the row
                    String separator = getSeparatorClass(row, mediaRow, isTFoot);
                    if (separator != null) {
                        newRow.addClass(separator);
                    }

                    // get the row and media row columns
                    Elements mediaColumns = mediaRow.children();
                    if (mediaColumns == null || mediaColumns.size() == 0) {
                        // no columns => ignore the media row
                        continue;
                    }
                    
                    // process the media row columns
                    int mediaColumnCount = mediaColumns.size();
                    for(int i = 0; i < mediaColumnCount; i++) {

                        // get the column
                        Element mediaColumn = mediaColumns.get(i);

                        // decide if the row has text and process accordingly
                        String  text    = mediaColumn.text();
                        boolean complex = text != null && ! text.equals("") ? true : false;

                        // get the column id and the column node
                        int id = (mediaColumn.hasAttr("id") ? Integer.parseInt(mediaColumn.attr("id")) : 1) - 1;
                        Element column = id < 0 || columns.size() <= id ? null : columns.get(id);
                        
                        // prepare the column
                        Element newColumn = null;

                        // process the column
                        if (complex) {

                            // the text is the column text
                            String colText = column != null ? column.text() : null;
                            if (colText != null && ! colText.equals("")) {

                                // replace "{cell}" with the column text
                                text = text.replaceAll("\\{cell\\}", colText);

                                // get the head text
                                String headText = null;
                                Element headEntry = getNthChild(headRow, id);
                                if (headEntry != null) {
                                    headText = headEntry.text();
                                }

                                // replace "{head}" with the header text
                                text = text.replaceAll("\\{head\\}", headText != null ? headText : "");

                                // create the new column
                                newColumn = createElem(doc, column != null ? column.tagName() : "td", text,
                                                       combineStyles(column, mediaColumn));
                            }

                        } else {
                            // the text is the column text
                            text = column != null ? column.text() : null;

                            // create the new column
                            newColumn = createElem(doc, column != null ? column.tagName() : "td", text,
                                                   combineStyles(column, mediaColumn));
                        }

                        // if this is the last column and we have less than colCount columns => add a column span
                        if (newColumn != null &&  (i + 1) == mediaColumnCount && mediaColumnCount < colCount) {
                            newColumn.attr("colspan", String.valueOf(colCount - i));
                        }

                        newRow.appendChild(newColumn);
                    }
                    
                    // if not allowing empty rows and the row is empty => discard the row
                    if ( ! allowEmpty && newRow.children().size() == 0) {
                        newRow = null;
                    }

                    // append the new row to the part
                    if (newRow != null) {
                        newPart.appendChild(newRow);
                    }
                }

            
            } else {

                // create the new row
                Element newRow = createElem(doc, "tr", null, getStyle(row));

                // get the fill (if any) and append it to the row
                String filler = getFillerClass(row);
                if (filler != null) {
                    newRow.addClass(filler);
                }

                // get the separator (if any) and append it to the row
                String separator = getSeparatorClass(row, isTFoot);
                if (separator != null) {
                    newRow.addClass(separator);
                }

                // copy all columns into the new row
                Elements columns = row.children();
                for(Element column : columns) {
                    newRow.appendChild(column);
                }

                // append the new row to the part
                newPart.appendChild(newRow);
            }
        }

        return newPart;
    }

    /**
     * Given a primary and secondary elements, get the filler class
     * @param primary the primary element
     * @param secondary the secondary element
     * @return the separator class
     */
    private String getFillerClass(Element primary, Element secondary) {

        // default is no filler class
        String filler = null;

        // get the filler for the primary
        if (filler == null) {
            filler = getFillerClass(primary);
        }

        // get the filler for the secondary
        if (filler == null) {
            filler = getFillerClass(secondary);
        }

        return filler;
    }

    /**
     * Given an element, returns a filler class depending on whether the element has the
     * <em>fill</em> attribute and it's value
     * @param elem the element to check for filler
     * @return the filler class
     */
    private String getFillerClass(Element elem) {

        // default is no filler class
        String filler = null;
        
        // get the fill attribute and decide the class (if the attribute is present)
        String fill   = elem.attr("fill");
        if (fill != null && ! "".equals(fill) ) {
            if (fill.equals("-") || fill.equals("_")) {             // "-", "_"
                filler = "filler_dashed";
            }
            if (filler == null) {                                   // "." or anything else
                filler = "filler_dotted";
            }
        }
        
        return filler;
    };

    /**
     * Given a primary and secondary elements, get the separator class
     * @param primary the primary element
     * @param secondary the secondary element
     * @param defaultToPre in case of <em>separator="true"</em> default to pre or post
     * @return the separator class
     */
    private String getSeparatorClass(Element primary, Element secondary, boolean defaultToPre) {

        // default is no separator class
        String separator = null;

        // get the separator for the primary
        if (separator == null) {
            separator = getSeparatorClass(primary, defaultToPre);
        }

        // get the separator for the secondary
        if (separator == null) {
            separator = getSeparatorClass(secondary, defaultToPre);
        }
        
        return separator;
    }

    /**
     * Given an element, returns a separator class depending on whether the element has the
     * <em>separator</em> attribute and it's value
     * @param elem the element to check for separator
     * @param defaultToPre in case of <em>separator="true"</em> default to pre or post
     * @return the separator class
     */
    private String getSeparatorClass(Element elem, boolean defaultToPre) {

        // default is no separator class
        String separator = null;
        
        // get the fill attribute and decide the class (if the attribute is present)
        String sep   = elem.attr("separator");
        if (sep != null && ! "".equals(sep) ) {
            if (sep.equalsIgnoreCase("true")) {
                separator = defaultToPre ? "separator_pre" : "separator_post";
            }
            if (sep.equalsIgnoreCase("both")) {
                separator = "separator_both";
            }
            if (sep.equalsIgnoreCase("pre")) {
                separator = "separator_pre";
            }
            if (sep.equalsIgnoreCase("post")) {
                separator = "separator_post";
            }
            if (separator == null) {                                // for anything else, we go with a simple separator
                separator = defaultToPre ? "separator_pre" : "separator_post";
            }
        }
        
        return separator;
    }

    /**
     * Returns the nth-child of the element. It can return children from the end of the
     * list by passing negative index values
     * @param elem the parent element
     * @param index the element index (negative values start at the end of the list toward the front)
     * @return the found element or null if not found
     */
    private Element getNthChild(Element elem, int index) {

        // get all the elements with the tag
        Elements elements = elem.children();
        if (elements == null || elements.size() == 0) {
            return null;
        }

        // return the selected index (negative or positive)
        if (index < 0) {
            index = elements.size() - index;
            return(index < 0 || elements.size() <= index ? null : elements.get(index));
        }
        return(index < elements.size() ? elements.get(index) : null);
    }
    
    /**
     * Returns the nth-child of the element with the given tag name. It can return children from the end of the
     * list by passing negative index values
     * @param elem the parent element
     * @param tagName the tag name to look for
     * @param index the element index (negative values start at the end of the list toward the front)
     * @return the found element or null if not found
     */
    private Element getNthChild(Element elem, String tagName, int index) {

        // get all the elements with the tag
        Elements elements = elem.getElementsByTag(tagName);
        if (elements == null || elements.size() == 0) {
            return null;
        }

        // return the selected index (negative or positive)
        if (index < 0) {
            index = elements.size() - index;
            return(index < 0 || elements.size() <= index ? null : elements.get(index));
        }
        return(index < elements.size() ? elements.get(index) : null);
    };

    /**
     * Returns the first child of the element with the given tag name.
     * @param elem the parent element
     * @param tagName the tag name to look for
     * @return the found element or null if not found
     */
    private Element getFirstChild(Element elem, String tagName) {

        return getNthChild(elem, tagName, 0);
    }

    /**
     * Returns the style attribute of an element (if it has it) or null
     * @param elem the element
     * @return the style or null
     */
    private String getStyle(Element elem) {
        
        return(elem != null && elem.hasAttr("style") ? elem.attr("style") : null);
    }
    
    /**
     * Combines the style attributes of two elements (each one being possibly empty), giving priority to the style
     * of the primary element (i.e: it will appear last so that it overrides the secondary styles)
     * @param primary the primary element
     * @param secondary the secondary element
     * @return the combined styles
     */
    private String combineStyles(Element primary, Element secondary) {
        
        // get the styles of both elements
        String pStyle = primary   != null && primary.hasAttr("style")   ? primary.attr("style")   : null;
        String sStyle = secondary != null && secondary.hasAttr("style") ? secondary.attr("style") : null;

        // create the style
        String style = null;
        
        // compose the style
        int cond = (pStyle != null ? 0x02 : 0x00) | (sStyle != null ? 0x01 : 0x00);
        switch (cond) {
            case 0x03:          // both styles present
                style = sStyle + " ; " + pStyle;
                break;
            case 0x02:          // only primary style present
                style = pStyle;
                break;
            case 0x01:          // only secondary style present
                style = sStyle;
                break;
        }
        
        return style;
    }

    /**
     * Converts a Cyberbank Devices print <em>style</em> definition into a CSS style.
     * 
     * <strong>IMPORTANT</strong>: Cyberbank Devices styles are a very reduced version of styles, so the parsing code is simple.
     * @param style the style
     * @return the converted style
     */
    private String convertStyle(String style) {

        // sanity check
        if (style == null || "".equals(style)) {
            return null;
        }

        // split the style using ";" as separator
        List<String> parts = Arrays.asList(style.split(";"));

        // iterate the parts, skiping the "font-variant" and copying the rest
        StringBuilder rebuilt = new StringBuilder();
        String sep = "";
        for(String part : parts) {

            // split in the attribute and value pairs
            String pair[] = part.split(":");
            if (pair[0] == null || "font-variant".equalsIgnoreCase(pair[0].trim())) {
                continue;
            }

            // add the separator and the part
            rebuilt.append(sep).append(part.trim());

            // separator is now "; "
            sep = "; ";
        }

        return rebuilt.toString();
    }

    /**
     * Converts a Cyberbank Devices print <em>style</em> <strong>font-variant</strong> into a class name.
     * 
     * <strong>IMPORTANT</strong>: what we look for is of the form <em>style="font-variant: 2h1w; "</em>

     * @param style the style
     * @return the class name
     */
    private String hasFontVariant(String style) {

        // sanity check
        if (style == null || "".equals(style)) {
            return null;
        }

        // split the style using ";" as separator
        List<String> parts = Arrays.asList(style.split(";"));

        // iterate the parts, looking for "font-variant" and, if found, return the class name
        for(String part : parts) {

            // split in the attribute and value pairs
            String pair[] = part.split(":");
            if (pair[0] != null && "font-variant".equalsIgnoreCase(pair[0].trim())) {

                // if we have a value, return that (trimmed)
                if (pair.length > 1 && pair[1] != null) {
                    return("variant-" + pair[1].trim());
                }
                
                continue;
            }
        }

        return null;
    }
    
    /**
     * Creates a simple HTML node
     * @param doc the document the element will belong to
     * @param tagName the tag name for the element
     * @return the created element
     */
    private Element createElem(Document doc, String tagName) {

        return createElem(doc, tagName, null, null, null);
    }

    /**
     * Creates a simple HTML node, with a possible text
     * @param doc the document the element will belong to
     * @param tagName the tag name for the element
     * @param text the (optional) text to put to the node
     * @return the created element
     */
    private Element createElem(Document doc, String tagName, String text) {

        return createElem(doc, tagName, text, null, null);
    }

    /**
     * Creates a simple HTML node with a style attribute
     * @param doc the document the element will belong to
     * @param tagName the tag name for the element
     * @param style the value to set for the style attribute
     * @return the created element
     */
    private Element createElem(Document doc, String tagName, String text, String style) {

        return createElem(doc, tagName, text, "style", style);
    }

    /**
     * Creates a simple HTML node, with a possible text and style
     * @param doc the document the element will belong to
     * @param tagName the tag name for the element
     * @param text the (optional) text to put to the node
     * @param style the (optional) style to set to the node
     * @return the created element
     */
    private Element createElem(Document doc, String tagName, String text, String attribName, String attribValue) {

        // create the element
        Element elem = doc.createElement(tagName);

        // set the text (if present)
        if (text != null) {
            elem.text(text);
        }
        
        // set the style attribute (if present)
        if (attribName != null) {
            elem.attr(attribName, attribValue != null ? attribValue : "");
        }
        
        return elem;
    }
}
