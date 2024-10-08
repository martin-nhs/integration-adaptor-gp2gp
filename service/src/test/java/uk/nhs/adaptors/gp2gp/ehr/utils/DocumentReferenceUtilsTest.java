package uk.nhs.adaptors.gp2gp.ehr.utils;

import org.apache.tika.mime.MimeTypeException;
import org.hl7.fhir.dstu3.model.Attachment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.adaptors.gp2gp.ehr.exception.EhrMapperException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentReferenceUtilsTest {
    private static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain";
    private static final String NARRATIVE_STATEMENT_ID = "3b24b89b-fd14-49f9-ba12-3b4212b60080";
    private static final String FILE_MISSING_ATTACHMENT_TITLE = "Reason why file is missing";
    private static final String UNSUPPORTED_CONTENT_TYPE = "unknown/unknown";
    private static final String INVALID_CONTENT_TYPE = "invalid content type";
    private static final String TEXT_HTML = "text/html";
    private static final String APPLICATION_XML = "application/xml";

    @ParameterizedTest
    @CsvSource({
        "text/plain,.txt",
        "text/html,.html",
        "application/pdf,.pdf",
        "text/xml,.xml",
        "application/xml,.xml",
        "text/rtf,.rtf",
        "audio/mpeg,.mp3",
        "image/png,.png",
        "image/gif,.gif",
        "image/jpeg,.jpg",
        "image/tiff,.tiff",
        "video/mpeg,.mpeg",
        "application/msword,.doc",
        "application/octet-stream,.bin",
        "audio/basic,.au",
        "audio/x-au,.au",
        "application/x-hl7,.xml,",
        "application/pgp-signature,.pgp",
        "video/msvideo,.avi",
        "application/pgp,.pgp",
        "application/x-shockwave-flash,.swf",
        "application/x-rar-compressed,.rar",
        "video/x-msvideo,.avi",
        "audio/wav,.wav",
        "application/hl7-v2,.xml",
        "audio/x-wav,.wav",
        "application/vnd.ms-excel,.xls",
        "audio/x-aiff,.aif",
        "audio/wave,.wav",
        "application/pgp-encrypted,.pgp",
        "video/x-ms-asf,.asf",
        "image/x-windows-bmp,.bmp",
        "video/3gpp2,.3g2",
        "application/x-netcdf,.cdf",
        "video/x-ms-wmv,.wmv",
        "application/x-rtf,.rtf",
        "application/x-mplayer2,.asx",
        "chemical/x-pdb,.pdb",
        "text/csv,.csv",
        "image/x-pict,.pct",
        "audio/vnd.rn-realaudio,.rm",
        "text/css,.css",
        "video/quicktime,.mov",
        "video/mp4,.mp4",
        "multipart/x-zip,.zip",
        "application/pgp-keys,.pgp",
        "audio/x-mpegurl,.m3u",
        "audio/x-ms-wma,.wma",
        "chemical/x-mdl-sdfile,.sdf",
        "application/x-troff-msvideo,.avi",
        "application/x-compressed,.zip",
        "image/svg+xml,.svg",
        "chemical/x-mdl-molfile,.mol",
        "application/EDI-X12,.edi",
        "application/postscript,.ps",
        "application/xhtml+xml,.xhtml",
        "video/x-flv,.flv",
        "application/x-zip-compressed,.zip",
        "application/hl7-v2+xml,.xml",
        "application/vnd.openxmlformats-package.relationships+xml,.rels",
        "video/x-ms-vob,.vob",
        "application/x-gzip,.gz",
        "audio/x-pn-wav,.wav",
        "application/msoutlook,.msg",
        "video/3gpp,.3g2",
        "application/cdf,.cdf",
        "application/EDIFACT,.edi",
        "application/x-cdf,.cdf",
        "application/x-pgp-plugin,.pgp",
        "audio/x-au,.au",
        "application/dicom,.dcm",
        "application/EDI-Consent,.edi",
        "application/zip,.zip",
        "application/json,.json",
        "application/x-pkcs10,.p10",
        "application/pkix-cert,.cer",
        "application/x-pkcs12,.p12",
        "application/x-pkcs7-mime,.p7m",
        "application/pkcs10,.p10",
        "application/x-x509-ca-cert,.cer",
        "application/pkcs-12,.p12",
        "application/pkcs7-signature,.p7s",
        "application/x-pkcs7-signature,.p7a",
        "application/pkcs7-mime,.p7m"
    })
    void When_SupportedMimeTypeIsProvided_Expect_CorrectFileExtensionIsGenerated(String contentType, String fileExtension) {
        var filename = DocumentReferenceUtils.buildAttachmentFileName(
            NARRATIVE_STATEMENT_ID, new Attachment().setContentType(contentType));

        assertThat(filename).endsWith(fileExtension);
    }

    @Test
    void When_UnsupportedMimeTypeIsProvided_Expect_Exception() {
        assertThatThrownBy(() -> DocumentReferenceUtils.buildAttachmentFileName(
            NARRATIVE_STATEMENT_ID, new Attachment().setContentType(UNSUPPORTED_CONTENT_TYPE)))
            .isInstanceOf(EhrMapperException.class)
            .hasMessage("Unsupported Content-Type: unknown/unknown");
    }

    @Test
    void When_InvalidMimeTypeIsProvided_Expect_Exception() {
        assertThatThrownBy(() -> DocumentReferenceUtils.buildAttachmentFileName(
            NARRATIVE_STATEMENT_ID, new Attachment().setContentType(INVALID_CONTENT_TYPE)))
            .isInstanceOf(EhrMapperException.class)
            .hasMessage("Unhandled exception while parsing Content-Type: invalid content type")
            .hasCauseInstanceOf(MimeTypeException.class);
    }

    @Test
    void When_ExtractingContentType_Expect_AttachmentContentTypeIsReturned() {
        var attachment = new Attachment().setContentType(TEXT_PLAIN_CONTENT_TYPE);
        assertThat(DocumentReferenceUtils.extractContentType(attachment))
            .isEqualTo(TEXT_PLAIN_CONTENT_TYPE);
    }

    @Test
    void When_ExtractingMissingContentType_Expect_Exception() {
        var attachment = new Attachment();
        assertThatThrownBy(() -> DocumentReferenceUtils.extractContentType(attachment))
            .isInstanceOf(EhrMapperException.class)
            .hasMessage("documentReference.content[0].attachment is missing contentType");
    }

    @Test
    void When_BuildingFileNameIfTitleIsPresent_Expect_MissingAttachmentFileNameIsGenerated() {
        var attachment = new Attachment()
            .setContentType(TEXT_HTML)
            .setTitle(FILE_MISSING_ATTACHMENT_TITLE);

        assertThat(DocumentReferenceUtils.buildAttachmentFileName(NARRATIVE_STATEMENT_ID, attachment))
            .isEqualTo("AbsentAttachment3b24b89b-fd14-49f9-ba12-3b4212b60080.txt");
    }

    @Test
    void When_BuildingFileNameForMissingAttachment_Expect_MissingAttachmentFileNameWithTxtExtensionGenerated() {
        assertThat(DocumentReferenceUtils.buildMissingAttachmentFileName(NARRATIVE_STATEMENT_ID))
            .isEqualTo("AbsentAttachment3b24b89b-fd14-49f9-ba12-3b4212b60080.txt");
    }

    @Test
    void When_BuildingFileNameForPresentAttachment_Expect_MissingAttachmentFileNameWithTxtExtensionGenerated() {
        assertThat(DocumentReferenceUtils.buildPresentAttachmentFileName(NARRATIVE_STATEMENT_ID, APPLICATION_XML))
            .isEqualTo("3b24b89b-fd14-49f9-ba12-3b4212b60080.xml");
    }

    @Test
    void When_BuildingFileNameIfTitleIsAbsent_Expect_FileNameIsGenerated() {
        var attachment = new Attachment()
            .setContentType(TEXT_PLAIN_CONTENT_TYPE);

        assertThat(DocumentReferenceUtils.buildAttachmentFileName(NARRATIVE_STATEMENT_ID, attachment))
            .isEqualTo("3b24b89b-fd14-49f9-ba12-3b4212b60080.txt");
    }
}
