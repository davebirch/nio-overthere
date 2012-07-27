package com.xebialabs.overthere.nio.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import com.google.common.base.Splitter;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public class OvertherePath implements Path {

    private OverthereFileSystem fileSystem;

    private List<String> segments;

    private boolean absolute;
    private final Splitter pathSplitter;

    OvertherePath(OverthereFileSystem fileSystem, String path) {
        this.fileSystem = fileSystem;
        String sep = fileSystem.getSeparator();
        this.absolute = path.startsWith(sep);
        pathSplitter = Splitter.on(sep).omitEmptyStrings();
        this.segments = newArrayList(pathSplitter.split(path));
    }

    OvertherePath(OverthereFileSystem fileSystem, List<String> segments, boolean absolute) {
        this.fileSystem = fileSystem;
        this.segments = segments;
        this.absolute = absolute;
        pathSplitter = Splitter.on(fileSystem.getSeparator()).omitEmptyStrings();
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public Path getRoot() {
        return absolute ? fileSystem.getRoot() : null;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public Path getFileName() {
        int last = segments.size();
        return slicePath(last - 1, last, false);
    }

    @Override
    public Path getParent() {
        return slicePath(0, segments.size() - 1, absolute);
    }

    @Override
    public int getNameCount() {
        return segments.size();
    }

    @Override
    public Path getName(int index) {
        checkArgument(index >= 0 && index < segments.size(), "Cannot call getName with index = %s on path: %s", index, toString());
        return slicePath(index, index + 1, false);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        checkArgument(beginIndex >= 0 && beginIndex < segments.size(), "Cannot call subpath with beginIndex = %s on path: %s", beginIndex, toString());
        checkArgument(endIndex > beginIndex && endIndex <= segments.size(), "Cannot call subpath with endIndex = %s on path: %s", endIndex, toString());
        return slicePath(beginIndex, endIndex, false);
    }

    private Path slicePath(int beginIndex, int endIndex, boolean absolute) {
        if (segments.isEmpty()) {
            return null;
        }

        return new OvertherePath(fileSystem, segments.subList(beginIndex, endIndex), absolute);
    }

    @Override
    public boolean startsWith(Path other) {
        if (!(other instanceof OvertherePath)) {
            return false;
        }

        OvertherePath otherPath = (OvertherePath) other;

        if (!otherPath.getFileSystem().equals(fileSystem)
                || otherPath.segments.size() > segments.size()
                || otherPath.absolute != absolute) {
            return false;
        }

        for (int i = 0; i < otherPath.segments.size(); i++) {
            if (!otherPath.segments.get(i).equals(segments.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean startsWith(String other) {
        return startsWith(fileSystem.getPath(other));
    }

    @Override
    public boolean endsWith(Path other) {
        if (!(other instanceof OvertherePath)) {
            return false;
        }

        OvertherePath otherPath = (OvertherePath) other;

        int otherSize = otherPath.segments.size();
        int size = segments.size();
        if (!otherPath.getFileSystem().equals(fileSystem)
                || otherSize > size
                || (otherSize == size && otherPath.absolute && !absolute)) {
            return false;
        }

        for (int i = 0; i < otherSize; i++) {
            if (!otherPath.segments.get(otherSize - i - 1).equals(segments.get(size - i - 1))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(fileSystem.getPath(other));
    }

    @Override
    public Path normalize() {
        Stack<String> filteredSegments = new Stack<String>();
        for (String segment : segments) {
            if (".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                if (!filteredSegments.isEmpty()) {
                    filteredSegments.pop();
                }
                continue;
            }
            filteredSegments.push(segment);
        }

        return new OvertherePath(fileSystem, newArrayList(filteredSegments), absolute);
    }

    @Override
    public Path resolve(Path other) {
        if (other.isAbsolute()) {
            return other;
        }

        List<String> strings = newArrayList(segments);
        for (int i = 0; i < other.getNameCount(); i++) {
            strings.add(other.getName(i).toString());
        }


        return new OvertherePath(fileSystem, strings, absolute);
    }

    @Override
    public Path resolve(String other) {
        return resolve(new OvertherePath(fileSystem, other));
    }

    @Override
    public Path resolveSibling(Path other) {
        return getParent().resolve(other);
    }

    @Override
    public Path resolveSibling(String other) {
        return getParent().resolve(other);
    }

    @Override
    public Path relativize(Path other) {
        if (absolute ^ other.isAbsolute()) {
            throw new IllegalArgumentException(format("Path [%s] and [%s] are not both (not) absolute", this, other));
        } else if (this.equals(other)) {
            return fileSystem.getPath("");
        }

        return null;
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toAbsolutePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        return new Iterator<Path>() {
            private int i = 0;
            @Override
            public boolean hasNext() {
                return (i < getNameCount());
            }
            @Override
            public Path next() {
                if (i < getNameCount()) {
                    Path result = getName(i);
                    i++;
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        String sep = getFileSystem().getSeparator();
        return (absolute ? sep : "") + on(sep).join(segments);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof OvertherePath)) return false;

        final OvertherePath other = (OvertherePath) o;
        return fileSystem.equals(other.fileSystem) && absolute == other.absolute && segments.equals(other.segments);

    }

    @Override
    public int hashCode() {
        int result = fileSystem != null ? fileSystem.hashCode() : 0;
        result = 31 * result + (segments != null ? segments.hashCode() : 0);
        result = 31 * result + (absolute ? 1 : 0);
        return result;
    }
}
