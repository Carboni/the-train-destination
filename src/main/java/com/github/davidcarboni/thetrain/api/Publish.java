package com.github.davidcarboni.thetrain.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.thetrain.json.Result;
import com.github.davidcarboni.thetrain.json.Transaction;
import com.github.davidcarboni.thetrain.storage.Publisher;
import com.github.davidcarboni.thetrain.storage.Transactions;
import com.github.davidcarboni.thetrain.upload.EncryptedFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * API to publish a file within an existing {@link Transaction}.
 */
@Api
public class Publish {

    @POST
    public Result addFile(HttpServletRequest request,
                          HttpServletResponse response) throws IOException, FileUploadException {

        Transaction transaction = null;
        String message = null;
        boolean error = false;

        try {
            // Record the start time
            Date startDate = new Date();

            // Get the file first because request.getParameter will consume the body of the request:
            Path file = getFile(request);

            // Now get the parameters:
            String transactionId = request.getParameter("transactionId");
            String uri = request.getParameter("uri");
            String encryptionPassword = request.getParameter("encryptionPassword");

            // Validate parameters
            if (StringUtils.isBlank(transactionId) || StringUtils.isBlank(uri)) {
                response.setStatus(HttpStatus.BAD_REQUEST_400);
                error = true;
                message = "Please provide transactionId and uri parameters.";
            }

            // Get the transaction
            transaction = Transactions.get(transactionId, encryptionPassword);
            if (transaction == null) {
                response.setStatus(HttpStatus.BAD_REQUEST_400);
                error = true;
                message = "Unknown transaction " + transactionId;
            }

            if (!error) {
                // Publish
                String sha = Publisher.addFile(transaction, uri, file, startDate);
                if (StringUtils.isNotBlank(sha)) {
                    message = "Published " + uri;
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    error = true;
                    message = "Sadly " + uri + " was not published.";
                }
            }

        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            error = true;
            message = ExceptionUtils.getStackTrace(e);
        }

        return new Result(message, error, transaction);
    }


    /**
     * Handles reading the uploaded file.
     *
     * @param request The http request.
     * @return A temp file containing the file data.
     * @throws IOException If an error occurs in processing the file.
     */
    Path getFile(HttpServletRequest request)
            throws IOException {
        Path result = null;

        // Set up the objects that do all the heavy lifting
        EncryptedFileItemFactory factory = new EncryptedFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            // Read the items - this will save the values to temp files
            for (FileItem item : upload.parseRequest(request)) {
                if (!item.isFormField()) {
                    // Write file to local temp file
                    result = Files.createTempFile("upload", ".file");
                    item.write(result.toFile());
                    // TODO: DiskFileItemFactory writes uploads to disk if they are over a certain size,
                    // TODO: so we delete the FileItem as soon as it's processed to minimise exposure:
                    item.delete();
                }
            }
        } catch (Exception e) {
            // item.write throws a general Exception, so specialise it by wrapping with IOException
            throw new IOException("Error processing uploaded file", e);
        }

        return result;
    }
}