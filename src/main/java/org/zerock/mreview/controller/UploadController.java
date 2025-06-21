package org.zerock.mreview.controller;


import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.mreview.dto.UploadResultDTO;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@RestController
@Log4j2
public class UploadController {

    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @PostMapping("/uploadAjax")
    public ResponseEntity<List<UploadResultDTO>> uploadFile(MultipartFile[] uploadFiles){

        List<UploadResultDTO> resultDTOList = new ArrayList<>();

        for(MultipartFile uploadFile : uploadFiles){
            if(uploadFile.getContentType().startsWith("image") == false){
                log.warn("this file is not image type");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // 전체 경로 C:\\example\example.txt 이런식으로
            String originalName = uploadFile.getOriginalFilename();
            // 마지막 \이후의 값 반환 --> 실제 파일 이름 example.txt 이런식으로
            String fileName = originalName.substring(originalName.lastIndexOf("\\")+ 1);

            log.info("filename: " + fileName);

            //날짜 폴더 생성
            String folderPath = makeFolder();

            //UUID
            String uuid = UUID.randomUUID().toString();

            //이미지 저장 경로(날짜 경로 폴더)(문자열)
            String saveName = uploadPath + File.separator + folderPath +
                    File.separator + uuid + "_" + fileName;

            Path savePath = Paths.get(saveName); // 문자열 경로를 Path객체로 바꿈
                                                 // 파일 작업(복사,이동,삭제 등) 작업 가능

            try {
                uploadFile.transferTo(savePath); // 업로드된 파일을 지정 위치에 저장,복사

                //섬네일 생성
                String thumbnailSaveName = uploadPath + File.separator + folderPath + File.separator
                        + "s_" + uuid + "_" + fileName;

                //섬네일 파일 이름은 중간에 s_로 시작
                File thumbnailFile = new File(thumbnailSaveName);

                //섬네일 생성 (savePath 경로에 thumbnailFile 저장)
                Thumbnailator.createThumbnail(savePath.toFile(), thumbnailFile,100,100);

                resultDTOList.add(new UploadResultDTO(fileName, uuid, folderPath));
            } catch (IOException e) { // transferTo는 무조건 예외처리 해야됨
                e.printStackTrace();
            }

        }
        return new ResponseEntity<>(resultDTOList, HttpStatus.OK);
    }
    //폴더생성
    private String makeFolder(){
        String str = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String folderPath = str.replace("//", File.separator);
        // 날짜가 2025/06/05 이런식인데 구분자를 변경함 --> 2025\06\05(폴더 경로 형식)

        File uploadPathFolder = new File(uploadPath,folderPath);
        // 기존 업로드 경로(uploadPath) 날짜 경로(folderPath) 둘을 합침

        if(uploadPathFolder.exists() == false){
            uploadPathFolder.mkdirs();
            // mkdir는 마지막 폴더만 생성 mkdirs는 중간 폴더도 생성
        }
        return folderPath; // 폴더 경로 반환
    }

    @GetMapping("/display")
    // 클라이언트는 filename 그 자체를 요청하는게 아닌
    // /display?fileName=2025%2F06%2F14%2F75f0e3eb-e224-..._셔츠.jpg 이런식으로 URL을 요청함
    // 클라이언트가 보내는 filename은 getImageURL로 만들어진 경로(DTO의 Filename이 아닌 ImageURL)
    // fileName은 클라이언트가 요청하는 상대 경로
    // String imageURL로 명칭 바꿔도 됨
    public ResponseEntity<byte[]> getFile(String fileName,String size) {
        ResponseEntity<byte[]> result = null;

        try{
            //DTO를 통해 저장된 이미지는 URL인코딩 되어 있음
            String srcFileName = URLDecoder.decode(fileName,"UTF-8");

            //디코딩된 파일 경로 로그 출력 (상대경로)
            log.info("fileName: " + srcFileName);

            // 실제 파일 경로
            File file = new File(uploadPath + File.separator + srcFileName);

            if(size != null && size.equals("1")) {
                file = new File(file.getParentFile(), file.getName().substring(2));
            }

            log.info("file: " + file);

            // HTTP응답에 넣을 객체
            HttpHeaders header = new HttpHeaders();
            // 파일의 확장자에 따라 MIME 타입을 자동으로 추측해서 Content-Type에 추가(jpg,png)
            header.add("Content-Type", Files.probeContentType(file.toPath()));

            // 클라이언트에게 보낼 응답 구성 (파일은 바이트형태로 header와 HTTP상태와 함께 구성)
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file),header,HttpStatus.OK);

        }   catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    @PostMapping("/removeFile")
    public ResponseEntity<Boolean> removeFile(String fileName) {

        String srcFileName = null;

        try {
            srcFileName = URLDecoder.decode(fileName,"UTF-8");
            // 원본 파일
            File file = new File(uploadPath + File.separator + srcFileName);
            // java.io.File에 delete 메서드가 있음
            boolean result = file.delete();

            // getParentFile() -> 원본 이미지가 있는 폴더 getName --> uuid 포함 파일 이름 추출
            File thumbnailFile = new File(file.getParentFile(), "s_" + file.getName());

            result = thumbnailFile.delete();

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(false,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
