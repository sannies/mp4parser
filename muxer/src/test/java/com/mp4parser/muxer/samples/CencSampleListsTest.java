package com.mp4parser.muxer.samples;

import com.mp4parser.muxer.samples.CencDecryptingSampleList;
import com.mp4parser.muxer.samples.CencEncryptingSampleList;
import com.mp4parser.tools.Hex;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.SampleImpl;
import com.mp4parser.tools.RangeStartMap;
import com.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by sannies on 1/9/14.
 */
public class CencSampleListsTest {

    String encryptedWorking = "00000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000DBF184112EB9111659712BAFCFF2AB249A7A06" +
            "19AAC29E6C1F2B5C4753D588F3142C51C9AF2CF1D92E8937C4FBC18D7AB212DA6951717F955907CA" +
            "D6BCFE086AB884CCFD8FB15E9915DB6AD76B296E4DF75D95D7A723B9FD967BF470B20130B8E0F59B" +
            "A30B8F5952638DE780179C95ABCA7A3453984B8BAB49189FA1A1FB5996872A681256DF43A616C790" +
            "BEF3041122FDF86ACAF3000127D77692B8DD6B44CB6AEEC7D3920932F607A3445CC03A6101E6648B" +
            "E4CF350EC5B1C195B4431779A2AF1877AD1499EE2611D3C8BB48172DCF7367F9F8F40E49AECE2DA7" +
            "44430E6757C6517D4198A9AB74CD0409CD4B8AAA1B98C2DF4A0EA02FCB6383FF34D84F10373CE3B1" +
            "DF6B0D29D1DB0154A3CB05E4D962BB636C1F65181B28CA5B831481E910E8D478497B459A4B940087" +
            "6115BDBDF99A20D799E372977105566FF2818AD6CF03253B42C7961C26A85A6180EAC0736C5A1F0F" +
            "2EC9833180FD82F14B63903F8AB8D006BD91E762AD5117DA4ECC33328E78373E3AC7913A11569983" +
            "E31F75D2A70000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000012C39E2DB870BD84029C" +
            "C00D3C123C53CFE967DED22459B413C8784D746C36E06B8C1EA14193C9B7A3652F6168D321939955" +
            "B86816E30E8C3D6CE8980D4E2FB4738CA7A21E14CF08D9E9723A59AF03B2961F8001B4409B66432D" +
            "1DD2BADE4BF4B50392645B6E2BE2FE93A6FD65D18AE60024CBFF010543C2D2E907812FE319A7CA5D" +
            "F6198A830FCB2E09D4D94638E909B0AEA344C6E5EE0C22546CDE272B54332BA83010BC570C738F82" +
            "78DFC4B63FDDAC2FB138ADF70C54606709E2B1035EE961DAF730F099EFA92D5AE4A87386DD54C1DC" +
            "919FE0E3D19C0D591EF9FEE498296D7DD967FE6112FCADABCB54D8F6AFF57C5BF9B032E8F790AD5A" +
            "A25EB003AC143E32E49A6D2D9D40990377A5E7EF76EC00F8D08B48333568505B6DA69B96CDBF7A7E" +
            "70886235FECC83389FC6EEAED3ECEFE4CB941A85C8D5B50A57A0BEB6A62918769ECAB79F9717741D" +
            "0118933E0611DEED910FE7ADD8B95355A9D4C43CD9328A3C585F5973BF16586FB4CFE0F419D56BC0" +
            "0C0BEAE83933ACB83E1DF2DC1B3E14FD5E5352E46EB823C33CF674D6292AAB377520EF22C60D60B1" +
            "7BB73DE3CD78215A52D3EDBDAD96D2B62F953CFF333AE9C221CDD9B498A03F886F761CBBC0C954B7" +
            "A57CFD0181094CFF0F5EBAC1056CF6DC0406D26BF612E2EE748CA2B52863889C81DD4D5419B009A6" +
            "6927B591060A0A4275044F03857138E4F761F1B58B980337BEF2957F1FF2E9B1351D517FB93F01C3" +
            "215EA09090A23EC3BFFC12F48E92101F6FE95C3C117998BA360050448B2753D0584A41F18EA25BCD" +
            "BDAA10DB623FBB53D3DB6F7D028990B411B9B5ECB11F423A9D2A4F8AFFC51E8B0C471545B4A545A7" +
            "0C7D89D42F0DC946A43389A6BF0168F546AC1667F43937DC4893DB329249";

    @Test
    public void test() throws IOException {
        SecretKey secretKey = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, "AES");

        List<Sample> clearSamples = Collections.<Sample>singletonList(
                new SampleImpl(ByteBuffer.wrap(new byte[1230])));

        CencSampleAuxiliaryDataFormat cencSampleAuxiliaryDataFormat = new CencSampleAuxiliaryDataFormat();
        cencSampleAuxiliaryDataFormat.pairs = new CencSampleAuxiliaryDataFormat.Pair[2];
        cencSampleAuxiliaryDataFormat.pairs[0] = cencSampleAuxiliaryDataFormat.createPair(101, 384);
        cencSampleAuxiliaryDataFormat.pairs[1] = cencSampleAuxiliaryDataFormat.createPair(105, 640);
        cencSampleAuxiliaryDataFormat.iv = new byte[16];


        CencEncryptingSampleList cencSamples =
                new CencEncryptingSampleList(
                        secretKey, clearSamples,
                        Collections.singletonList(cencSampleAuxiliaryDataFormat));

        Assert.assertEquals(1, cencSamples.size());
        Sample encSample = cencSamples.get(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        encSample.writeTo(Channels.newChannel(baos));
        Assert.assertEquals(encryptedWorking, Hex.encodeHex(baos.toByteArray()));
        Assert.assertEquals(encryptedWorking, Hex.encodeHex(encSample.asByteBuffer().array()));


    }

    @Test
    public void testMultipleKeysCencSubSample() throws IOException {
        testMultipleKeys("cenc", true);
    }

    @Test
    public void testMultipleKeysCbc1SubSample() throws IOException {
        testMultipleKeys("cbc1", true);
    }

    @Test
    public void testMultipleKeysCencFull() throws IOException {
        testMultipleKeys("cenc", false);
    }

    @Test
    public void testMultipleKeysCbc1Full() throws IOException {
        testMultipleKeys("cbc1", false);
    }

    public void testMultipleKeys(String encAlgo, boolean subSampleEncryption) throws IOException {

        SecretKey cek1 = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, "AES");
        SecretKey cek2 = new SecretKeySpec(new byte[]{2, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, "AES");

        RangeStartMap<Integer, SecretKey> keys = new RangeStartMap<Integer, SecretKey>();
        keys.put(0, cek1);
        keys.put(3, null);
        keys.put(5, cek2);


        List<Sample> clearSamples = Arrays.<Sample>asList(
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230])),
                new SampleImpl(ByteBuffer.wrap(new byte[1230]))
        );


        CencSampleAuxiliaryDataFormat cencAuxDef = new CencSampleAuxiliaryDataFormat();

        if (subSampleEncryption) {
            cencAuxDef.pairs = new CencSampleAuxiliaryDataFormat.Pair[2];
            cencAuxDef.pairs[0] = cencAuxDef.createPair(101, 384);
            cencAuxDef.pairs[1] = cencAuxDef.createPair(105, 640);
        }
        cencAuxDef.iv = new byte[16];

        CencSampleAuxiliaryDataFormat cencAuxPlain = new CencSampleAuxiliaryDataFormat();

        List<CencSampleAuxiliaryDataFormat> auxInfos = Arrays.asList(
                cencAuxDef, cencAuxDef, cencAuxDef,
                cencAuxPlain, cencAuxPlain,
                cencAuxDef, cencAuxDef, cencAuxDef, cencAuxDef);


        CencEncryptingSampleList cencSamples =
                new CencEncryptingSampleList(
                        keys, clearSamples, auxInfos, encAlgo);

        Assert.assertEquals(9, cencSamples.size());
        for (int i = 0; i < cencSamples.size(); i++) {

            CencDecryptingSampleList dec = new CencDecryptingSampleList(
                    new RangeStartMap<Integer, SecretKey>(0, keys.get(i)),
                    Collections.singletonList(cencSamples.get(i)),
                    Collections.singletonList(auxInfos.get(i)), encAlgo);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dec.get(0).writeTo(Channels.newChannel(baos));
            Assert.assertArrayEquals("Sample " + i + " can not be reconstructed", new byte[1230], baos.toByteArray());
        }

        CencDecryptingSampleList decryptingSampleList = new CencDecryptingSampleList(
                keys,
                cencSamples,
                auxInfos,
                encAlgo);

        for (int i = 0; i < cencSamples.size(); i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            decryptingSampleList.get(i).writeTo(Channels.newChannel(baos));
            Assert.assertArrayEquals("Sample " + i + " can not be reconstructed", new byte[1230], baos.toByteArray());
        }


    }
}
