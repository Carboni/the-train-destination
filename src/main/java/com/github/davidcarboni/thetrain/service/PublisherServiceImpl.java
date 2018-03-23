package com.github.davidcarboni.thetrain.service;

import com.github.davidcarboni.thetrain.json.Transaction;
import com.github.davidcarboni.thetrain.storage.Publisher;
import com.github.davidcarboni.thetrain.storage.Website;

import java.io.IOException;
import java.nio.file.Path;

public class PublisherServiceImpl implements PublisherService {

    @Override
    public Path websitePath() throws IOException {
        return Website.path();
    }

    @Override
    public boolean commit(Transaction transaction, Path website) throws IOException {
        return Publisher.commit(transaction, website);
    }
}