package com.hackathon.eot.service;

import com.hackathon.eot.model.constant.ImageRole;
import com.hackathon.eot.model.entity.ImageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileHandler {

    public List<ImageEntity> parseFileInfo(List<MultipartFile> files) throws Exception {
        // 반환할 파일 리스트
        List<ImageEntity> imageList = new ArrayList<>();

        // 전달되어 온 파일이 존재할 경우
        if(!CollectionUtils.isEmpty(files)) {
            // 파일명을 업로드 한 날짜로 변환하여 저장
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String current_date = now.format(dateTimeFormatter);

            // TODO: 추후에 S3에 저장될 수 있도록 구현
            // 프로젝트 디렉터리 내의 저장을 위한 절대 경로 설정
            // 경로 구분자 File.separator 사용
            String absolutePath = new File("").getAbsolutePath() + File.separator + File.separator;

            // 파일을 저장할 세부 경로 지정
            String path = "images" + File.separator + current_date;
            File file = new File(path);

            // 디렉터리가 존재하지 않을 경우
            if(!file.exists()) {
                boolean wasSuccessful = file.mkdirs();
            }

            // 다중 파일 처리
            for(MultipartFile multipartFile : files) {
                // 파일 확장자 추출
                String originalFileExtension;
                String contentType = multipartFile.getContentType();

                // 확장자명이 존재하지 않을 경우 처리x
                if(ObjectUtils.isEmpty(contentType)) break;
                else { // 확장자가 jpeg, png 인 파일들만 받아서 처리
                    if(contentType.contains("image/jpeg")) originalFileExtension = ".jpg";
                    else if(contentType.contains("image/png")) originalFileExtension = ".png";
                    else break; // 다른 확장자일 경우 처리 x
                }

                // 파일명 중복을 피하기 위해서 나노초까지 얻어와 파일명 지정
                String newFileName = System.nanoTime() + originalFileExtension;

                // ImageEntity 생성
                ImageEntity image = ImageEntity.of(path + File.separator + newFileName,
                        multipartFile.getOriginalFilename(), ImageRole.UPLOADED);

                // 리스트에 추가
                imageList.add(image);

                // 업로드 한 파일 데이터를 지정한 파일에 저장
                file = new File(absolutePath + path + File.separator + newFileName);
                multipartFile.transferTo(file);

                // 파일 권한 설정(쓰기, 읽기)
                file.setWritable(true);
                file.setReadable(true);
            }
        }
        return imageList;
    }
}
