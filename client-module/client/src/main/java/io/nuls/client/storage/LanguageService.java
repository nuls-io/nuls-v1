package io.nuls.client.storage;

import io.nuls.kernel.model.Result;

/**
 * 系统语言设置数据存储服务接口
 * @author: Charlie
 * @date: 2018/6/28
 */
public interface LanguageService {

    Result saveLanguage(String language);

    Result getLanguage();

}
