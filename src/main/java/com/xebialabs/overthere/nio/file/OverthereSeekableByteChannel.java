package com.xebialabs.overthere.nio.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.Closeables;

public class OverthereSeekableByteChannel implements SeekableByteChannel {

	private boolean open = true;
	
	private long position;
	private long size;
	private InputStream in;
	private OutputStream out;
	
	public OverthereSeekableByteChannel(Path file, InputStream in, OutputStream out) throws IOException {
		this.size = Files.size(file);
		this.position = 0;
		this.in = in;
		this.out = out;
	}
	
	@Override
	public boolean isOpen() {
		return this.open;
	}

	@Override
	public void close() throws IOException {
		Closeables.closeQuietly(in);
        Closeables.closeQuietly(out);
        
        this.open = false;

	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
        byte[] buf = new byte[dst.remaining()];
        int bytesRead = in.read(buf);
        dst.put(buf, 0, bytesRead);
        
        position += bytesRead;
        
        return bytesRead;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
        int remaining = src.remaining();
        out.write(src.array(), 0, remaining);
        src.position(remaining);
        return remaining;
	}

	@Override
	public long position() throws IOException {
		return position;
	}

	@Override
	public SeekableByteChannel position(long newPosition) throws IOException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public long size() throws IOException {
		return this.size;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		throw new UnsupportedOperationException();
	}

}
