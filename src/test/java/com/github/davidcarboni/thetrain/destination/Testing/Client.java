package com.github.davidcarboni.thetrain.destination.Testing;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.davidcarboni.thetrain.destination.helpers.PathUtils;
import com.github.davidcarboni.thetrain.destination.json.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by david on 21/08/2015.
 */
public class Client {

    public static void main(String[] args) throws IOException {

        Host host = new Host("http://localhost:8080");
        Path generated = Generator.generate();
        System.out.println("Generated content in: " + generated);

        try (Http http = new Http()) {

            String encryptionPassword = Random.password(8);

            // Set up a transaction
            Endpoint begin = new Endpoint(host, "begin").setParameter("encryptionPassword", encryptionPassword);
            Response<Result> beginResponse = http.post(begin, Result.class);
            check(beginResponse);
            String transactionId = beginResponse.body.transaction.id();

            // Publish some content
            Endpoint publish = new Endpoint(host, "publish").setParameter("transactionId", transactionId).setParameter("encryptionPassword", encryptionPassword);
            List<String> uris = PathUtils.listUris(generated);
            for (String uri : uris) {
                Path path = PathUtils.toPath(uri, generated);
                Endpoint endpoint = publish.setParameter("uri", uri);
                Response<Result> resultResponse = http.postFile(endpoint, path, Result.class);
                check(resultResponse);
            }

            // Commit the transaction
            Endpoint commit = new Endpoint(host, "commit").setParameter("transactionId", transactionId).setParameter("encryptionPassword", encryptionPassword);
            Response<Result> commitResponse = http.put(commit, null, Result.class);
            check(commitResponse);

            System.out.println("Committed transaction " + transactionId);
        }
    }

    static void check(Response<Result> response) {
        if (response.statusLine.getStatusCode() != 200) {
            throw new RuntimeException(response.statusLine + " : " + Serialiser.serialise(response.body));
        } else if (response.body.transaction.hasErrors()) {
            throw new RuntimeException(Serialiser.serialise(response.body.transaction));
        }
    }
}
