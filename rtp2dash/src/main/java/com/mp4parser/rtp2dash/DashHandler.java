package com.mp4parser.rtp2dash;

import com.mp4parser.streaming.StreamingTrack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import mpeg.dash.schema.mpd._2011.AdaptationSetType;
import mpeg.dash.schema.mpd._2011.MPDtype;
import mpeg.dash.schema.mpd._2011.ObjectFactory;
import mpeg.dash.schema.mpd._2011.PeriodType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

class DashHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    FullHttpRequest msg;
    /**
     * Buffer that stores the response content
     */
    private final StringBuilder buf = new StringBuilder();

    List<DashFragmentedMp4Writer> tracks;

    public DashHandler(List<DashFragmentedMp4Writer> tracks) {
        this.tracks = tracks;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws JAXBException {
        this.msg = msg;
        boolean keepAlive = HttpHeaderUtil.isKeepAlive(msg);
        if ("/Manifest.mpd".equals(msg.uri())) {

            MPDtype mpd = new MPDtype();
            PeriodType periodType = new PeriodType();
            mpd.getPeriod().add(periodType);
            Map<Long, AdaptationSetType> adaptationSetsMap = new HashMap<Long, AdaptationSetType>();
            for (DashFragmentedMp4Writer track : tracks) {
                AdaptationSetType adaptationSetType = adaptationSetsMap.get(track.getAdaptationSetId());
                if (adaptationSetType == null) {
                    adaptationSetType = new AdaptationSetType();
                    adaptationSetsMap.put(track.getAdaptationSetId(), adaptationSetType);
                    adaptationSetType.setId(track.getAdaptationSetId());
                }
                adaptationSetType.getRepresentation().add(track.getRepresentation());
            }
            for (AdaptationSetType adaptationSetType : adaptationSetsMap.values()) {
                periodType.getAdaptationSet().add(adaptationSetType);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.marshal(new ObjectFactory().createMPD(mpd), sw);

            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK,
                    Unpooled.copiedBuffer(sw.getBuffer(), CharsetUtil.UTF_8));

            response.headers().set("Content-Type", "application/dash+xml");

            if (keepAlive) {
                // Add 'Content-Length' header only for a keep-alive connection.
                response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }


            // Write the response.
            ctx.write(response);

        } else {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK,
                    Unpooled.copiedBuffer("Notting", CharsetUtil.UTF_8));

        }

        if (!keepAlive) {
            // If keep-alive is off, close the connection once the content is fully written.
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }


    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaderUtil.isKeepAlive(msg);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }


        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
