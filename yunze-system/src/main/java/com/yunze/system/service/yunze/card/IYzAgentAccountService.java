package com.yunze.system.service.yunze.card;

import java.util.List;
import java.util.Map;

public interface IYzAgentAccountService {

    public Map<String,Object> getList(Map map);

    public Map<String,Object>  save(Map map);

    public boolean  upd(Map map);

    public boolean  del(Map map);

    public List<String> is_exaAgentId(Map map);

}
