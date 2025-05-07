package psu.expresso.file;

import java.io.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileWriter {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void saveToFile(File file, String contents) {

        lock.writeLock().lock();

        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(file))) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
