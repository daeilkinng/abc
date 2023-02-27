package kr.dmove.woori.drm.service;

import SCSL.*;
import kr.dmove.woori.drm.config.Config;
import kr.dmove.woori.drm.config.Constants;
import org.apache.log4j.Logger;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;

@Named
public class SocamService {

    private final Logger logger = Logger.getLogger(SocamService.class);

    public void Enc(String srcFile, String dstFile, Config config) throws CryptoException {
        try {
//            for DEV
//            FileInputStream fis = new FileInputStream(new File(srcFile));
//            BufferedInputStream reader = new BufferedInputStream(fis);
//
//            FileOutputStream fos = new FileOutputStream(new File(dstFile));
//            BufferedInputStream writer = new BufferedInputStream(fos);
//
//            byte[] bytes = new byte[128];
//            int ch;
//            while ((ch = reader.read(bytes)) != -1) {
//                writer.write(bytes, 0, ch);
//            }
//            writer.flush();
//            writer.close();
//            reader.close();

            SLDsFile sFile = new SLDsFile();

            sFile.SettingPathForProperty(config.getProperties());

            sFile.SLDsInitDAC();
            sFile.SLDsAddUserDAC(config.getUserId(), config.getAuth(), 0, 0, 0);

            int retVal = sFile.SLDsEncFileDACV2(config.getKeyfile(), config.getSystem(), srcFile, dstFile, Constants.SOCAM_ENC_OPTION);
            logger.debug("========== [retVal] : " + retVal);
            if (retVal == 0) {
            } else {
                throw new CryptoException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("========== [stackTrace] : " + e.toString());
            for (int i = 0; i < 10; i++) {
                logger.debug("========== [stackTrace] : " + e.getStackTrace()[i].toString());
            }
            throw new CryptoException();
        }
    }

    public void Dec(String srcFile, String dstFile, Config config) throws CryptoException {
        try {
//            for DEV
//            FileInputStream fis = new FileInputStream(new File(srcFile));
//            BufferedInputStream reader = new BufferedInputStream(fis);
//
//            FileOutputStream fos = new FileOutputStream(new File(dstFile));
//            BufferedInputStream writer = new BufferedInputStream(fos);
//
//            byte[] bytes = new byte[128];
//            int ch;
//            while ((ch = reader.read(bytes)) != -1) {
//                writer.write(bytes, 0, ch);
//            }
//            writer.flush();
//            writer.close();
//            reader.close();

            SLDsFile sFile = new SLDsFile();

            sFile.SettingPathForProperty(config.getProperties());

            int retVal = sFile.CreatDecryptFileDAC(config.getKeyfile(), config.getUserId(), srcFile, dstFile);
            if (retVal == 0) {
                try {
                    FileInputStream fis = new FileInputStream(new File(dstFile));
                    logger.debug("file size : " + (int)fis.getChannel().size());
                    fis.close();
                }catch (Exception e) {
                    throw new CryptoException();
                }
                logger.debug("========== [Decrypt Success] : " + retVal);
            } else if (retVal == 36) {
                logger.debug("========== [Decrypt Success] : " + retVal);
            } else {
                logger.debug("========== [Decrypt Fail] : " + retVal);
                throw new CryptoException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("========== [stackTrace] : " + e.toString());
            for (int i = 0; i < 10; i++) {
                logger.debug("========== [stackTrace] : " + e.getStackTrace()[i].toString());
            }
            throw new CryptoException();
        }
    }

}
