package io.nuls.network.storage.service;

import io.nuls.network.entity.Node;
import java.util.List;
import java.util.Set;

public interface NetworkStorageService {

    List<Node> getLocalNodeList(int size);

    List<Node> getLocalNodeList(int size, Set<String> ipSet);

    void saveNode(Node node);

    void deleteNode(String nodeId);
}
