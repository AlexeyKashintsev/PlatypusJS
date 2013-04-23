/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.server.mina.platypus;

import com.eas.client.threetier.Response;
import com.eas.client.threetier.binary.PlatypusResponseWriter;
import com.eas.proto.ProtoWriter;
import java.io.ByteArrayOutputStream;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 *
 * @author pk, mg refactoring
 */
public class ResponseEncoder implements ProtocolEncoder {

    public ResponseEncoder() {
        super();
    }

    @Override
    public void encode(IoSession aSession, Object aResponse, ProtocolEncoderOutput output) throws Exception {
        if (aResponse instanceof Response) {
            Response rsp = (Response) aResponse;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ProtoWriter writer = new ProtoWriter(outStream);
            try {
                PlatypusResponseWriter.write(rsp, writer);
            } finally {
                writer.flush();
            }
            output.write(IoBuffer.wrap(outStream.toByteArray()));
        } else if (aResponse instanceof Signature) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ProtoWriter writer = new ProtoWriter(outStream);
            try {
                writer.putSignature();
            } finally {
                writer.flush();

            }
            output.write(IoBuffer.wrap(outStream.toByteArray()));
        }
    }

    @Override
    public void dispose(IoSession arg0) throws Exception {
        //nothing to dispose
    }
}