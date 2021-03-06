package anan.base.filemanage.controller;

import anan.base.core.enums.EnabledEnum;
import anan.base.core.exception.CoreException;
import anan.base.core.orm.ResponseResult;
import anan.base.core.service.DictOptionService;
import anan.base.core.util.ResultVOUtil;
import anan.base.core.vo.ResultVO;
import anan.base.filemanage.FileTable;
import anan.base.filemanage.config.FileConfig;
import anan.base.filemanage.enums.ResultEnum;
import anan.base.filemanage.form.FileForm;
import anan.base.filemanage.orm.File;
import anan.base.filemanage.service.FileService;
import anan.base.filemanage.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anan
 * Created on 2018/8/8.
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

  @Autowired
  private FileService fileService;

  @Autowired
  private FileConfig fileConfig;

  @Autowired
  private DictOptionService dictOptionService;



  /**
   * findAll
   * @return ResultVO
   */
  @GetMapping("")
  public ResultVO findAll(){
    List<File> all = fileService.findAll();
    return ResultVOUtil.success(all);
  }

  /**
   * findOne
   * @param id primary key
   * @return ResultVO
   */
  @GetMapping("/{id}")
  public ResultVO findOne(@PathVariable("id") Integer id){
    return ResultVOUtil.success(fileService.findOne(id));
  }

  /**
   * save
   * @param data :FileForm pojo
   * @return ResultVO
   */
  @ResponseBody
  @PostMapping(value = "",produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO save(@Valid @RequestBody FileForm data, BindingResult bindingResult){
    if (bindingResult.hasErrors()) {
      log.error("【文件管理】参数不正确, FileForm={}", data);
      throw new CoreException(ResultEnum.PARAM_ERROR.getCode(),
              bindingResult.getFieldError().getDefaultMessage());
    }
    fileService.save(data);
    return ResultVOUtil.success();

  }


  /**
   * update file info
   * @param data :FileForm pojo
   * @return ResultVO
   */
  @PutMapping("/{id}")
  public ResultVO update(@Valid @RequestBody FileForm data, @PathVariable("id") Integer id, BindingResult bindingResult){
    if (bindingResult.hasErrors() || null == data.getFileTypeId()) {
      log.error("【文件管理】参数不正确, FileForm={}", data);
      throw new CoreException(ResultEnum.PARAM_ERROR.getCode(),
              bindingResult.getFieldError().getDefaultMessage());
    }
    ResponseResult result = new ResponseResult();
    data.setId(id);
    fileService.update(data, result);
    if(result.hasErrors())
      return ResultVOUtil.error(ResultEnum.FAILURE.getCode(), result.getMessage());
    return ResultVOUtil.success();

  }


  /**
   * batch delete
   * @param id // @RequestBody Map<String,String> param {"id":"1,2,3,4,5"}
   *           // @PathVariable String id :"1,2,3,4,5", force: 1/true/0/false
   * @return ResultVO
   */
  @ResponseBody
  @RequestMapping(value = "/{id}/{force}", method = RequestMethod.DELETE)
  public ResultVO delete(@PathVariable("id") String id, @PathVariable("force") boolean force) {
    ResponseResult result = new ResponseResult();
    fileService.delete(id, force,result);
    if(result.hasMessages())
      return ResultVOUtil.error(ResultEnum.FILE_DELETE_SECTION.getCode(), result.getMessage());
    return ResultVOUtil.success();
  }


  /**
   * upload file
   * @param data : FileForm
   * @return ResultVO
   */
  @ResponseBody
  @PostMapping(value = "/io",produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
  public ResultVO upload(@RequestBody FileForm data){
    try {
      java.io.File file = FileUtil.uploadFile(data.getMulFile(), fileConfig.getUploadPath());

      data.setMd5(FileUtil.getMd5ByFile(file));
      data.setSize(file.length());
      data.setFilePath(file.getPath());
      data.setName(file.getName());

      val save  = fileService.save(data);
      return ResultVOUtil.success();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      log.info("【文件管理】文件上传出现异常：FileNotFoundException：：位置：HFileController.upload/POST ");
      return ResultVOUtil.error(ResultEnum.FILE_UPLOAD_EXCEPTION.getCode(), ResultEnum.FILE_UPLOAD_EXCEPTION.getMessage());
    }
  }

  /**
   * download file
   * @return ResultVO
   */
  @RequestMapping(value = "/io/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
  public ResponseEntity<InputStreamResource> download(@PathVariable("id") Integer id) throws Exception {

    val filePath =  fileService.findOne(id).getFilePath();
    val file = new FileSystemResource(filePath);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFilename()));
    headers.add("Pragma", "no-cache");
    headers.add("Expires", "0");

    try {
      return ResponseEntity
              .ok()
              .headers(headers)
              .contentLength(file.contentLength())
              .contentType(MediaType.parseMediaType("application/octet-stream"))
              .body(new InputStreamResource(file.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
      log.info("【文件管理】文件下载出现异常：IOException：：位置：HFileController.download/GET ");
    }
    return null;
  }

  /**
   * relation
   * @return
   */
  @GetMapping(path = "/relation")
  public ResultVO relation() {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("enabled", EnabledEnum.relation());
    data.put("fileType", this.dictOptionService.listByTypeForRelation(FileTable.file));
    return ResultVOUtil.success(data);
  }

}
