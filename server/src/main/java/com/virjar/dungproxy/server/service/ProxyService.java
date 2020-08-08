package com.virjar.dungproxy.server.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.virjar.dungproxy.server.model.ProxyModel;

public interface ProxyService {
    int create(ProxyModel proxyModel);

    int createSelective(ProxyModel proxyModel);

    ProxyModel findByPrimaryKey(Long id);

    int updateByPrimaryKey(ProxyModel proxyModel);

    int updateByPrimaryKeySelective(ProxyModel proxyModel);

    int deleteByPrimaryKey(Long id);

    int selectCount(ProxyModel proxyModel);

    List<ProxyModel> selectPage(ProxyModel proxyModel, Pageable Pageable);

    List<ProxyModel> find4availableupdate();

    List<ProxyModel> find4connectionupdate();

    void save(List<ProxyModel> draftproxys);

    ProxyModel selectByIpPort(String ip, int port);

    public List<ProxyModel> allAvailable();

    public List<ProxyModel> allAvailablePage(Pageable Pageable);
}