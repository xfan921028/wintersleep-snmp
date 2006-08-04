/*
 * Copyright 2005 Davy Verstappen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsmiparser.phase.oid;

import org.jsmiparser.util.problem.ProblemReporterFactory;
import org.jsmiparser.util.token.BigIntegerToken;
import org.jsmiparser.util.token.IdToken;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @todo Is this correct:
 * Global or per-module uniqueness of identifiers doesn't have to be checked here. The value assignment
 * checks take care of that.
 */
public class OidMgr {

    OidProblemReporter m_pr;
    Set m_ignorableIds = new HashSet();
    Map<String, OidNode> m_idNodeMap = new HashMap<String, OidNode>();
    Map<BigInteger, OidNode> m_standardSubIdMap = new HashMap<BigInteger, OidNode>();

    OidNode root_;

    public OidMgr(ProblemReporterFactory prf) {
        m_pr = prf.create(OidMgr.class.getClassLoader(), OidProblemReporter.class);

        root_ = create(null, new IdToken(null, OidNode.ROOT_NODE_NAME), new BigIntegerToken(null, false, "0"));

        // see http://asn1.elibel.tm.fr

        // TODO by default disable some of these shaky standard root names

        OidNode node0 = create(root_, new IdToken(null, "itu-t"), new BigIntegerToken(null, false, "0"));
        m_standardSubIdMap.put(node0.getValue(), node0);
        m_idNodeMap.put("itu-t", node0);
        m_idNodeMap.put("ccitt", node0);
        m_idNodeMap.put("itu-r", node0);
        m_idNodeMap.put("itu", node0);

        OidNode node1 = create(root_, new IdToken(null, "iso"), new BigIntegerToken(null, false, "1"));
        m_standardSubIdMap.put(node1.getValue(), node1);
        m_idNodeMap.put("iso", node1);

        OidNode node2 = create(root_, new IdToken(null, "joint-iso-itu-t"), new BigIntegerToken(null, false, "2"));
        m_standardSubIdMap.put(node2.getValue(), node2);
        m_idNodeMap.put("joint-iso-itu-t", node2);
        m_idNodeMap.put("joint-iso-ccitt", node2);
    }

    /*
    public OidNode getNode(OidNode parent, String id, BigInteger subId) {
        OidNode result = null;
        if (parent == null) {
            if (id != null) {
                result = findNode(id);
                if (result == null) {
                    // this is resolved later
                    //m_pr.error("ASN OID that starts with a name must have a first name that is already declared.", id);
                }
            } else {
                result = m_standardSubIdMap.get(subId);
                if (result == null) {
                    m_pr.reportInvalidOidStart(subId);
                }
            }
        }

        // we always make one, to avoid crashes
        if (result == null) {
            result = create(parent, id, subId);
        }
        return result;
    }
    */

    public OidNode registerNode(IdToken idToken, OidNode newNode) {
        if (newNode.m_idToken == null) {
            newNode.m_idToken = idToken;
        }

        OidNode result = null;
        OidNode oldNode = findNode(idToken.getId());
        if (oldNode != null) {
            oldNode.setOrCheckValueToken(newNode.m_valueToken);
            if (newNode.m_parent != null) {
                if (oldNode.m_parent == null) {
                    oldNode.m_parent = newNode.m_parent;
                    oldNode.m_parent.m_children.add(oldNode);
                } else {
                    if (newNode.m_parent != oldNode.m_parent) {
                        m_pr.reportDifferentParent(newNode.getLocation(), newNode.getId(),
                                newNode.getParent().getId(), newNode.getParent().getLocation(),
                                oldNode.getParent().getId(), oldNode.getParent().getLocation());
                        result = newNode;
                    }
                }
            }
            if (result == null) {
                result = oldNode;
                if (oldNode.m_parent != null) {
                    newNode.m_parent.m_children.remove(newNode);
                }
            }
            /*
            if (oldNode.m_valueToken == null && newNode.m_valueToken != null) {
                oldNode.m_valueToken = newNode.m_valueToken;
            } else if (newNode.equals(oldNode)) {
                result = oldNode;
            } else {
                m_pr.reportOidAlreadyRegistered(idToken.getLocation(), idToken.getId(), oldNode.getIdToken().getLocation());
            }
            */
        } else {
            //m_idNodeMap.put(newNode.getId(), newNode);
            m_idNodeMap.put(idToken.getId(), newNode);
            result = newNode;
        }

        return result;
    }

    public void check() {
        for (OidNode oidNode : m_idNodeMap.values()) {
            oidNode.check();
        }
    }

    /*
    public void resolveAll() {
        for (String id : m_idNodeMap.keySet()) {
            resolve(id);
//            OidNode node = (OidNode) entry.getValue();
//            OidNode nodeRoot = node.getRoot();
//            if (nodeRoot != root_)
//            {
//                use(id, node, nodeRoot);
//            }
        }
    }

    private OidNode resolve(String id) {
        OidNode node = findNode(id);
        if (node == null) {
            m_pr.reportCannotFindOidNode(id);
        } else {
            OidNode nodeRoot = node.getRoot();
            if (nodeRoot != root_) {
                if (nodeRoot.getValue() == null) {
                    OidNode n = resolve(nodeRoot.getId());
                    if (n != null) {
                        if (isResolved(n)) {
                            node.fixStart(n);
                        } else {
                            // error message here will only be confusing
                        }
                    } else {
                        // error message already printed by call to use
                    }
                } else {
                    m_pr.reportCannotResolveUnresolvedOid(id);
                }
            }
        }
        return node;
    }
    */

    /**
     * Resolves the starting node of an oid sequence.
     *
     * @param idToken
     * @param valueToken
     * @return
     */
    public OidNode resolveStart(IdToken idToken, BigIntegerToken valueToken) {
        assert(idToken != null || valueToken != null);
        OidNode result = null;
        if (idToken != null) {
            result = m_idNodeMap.get(idToken.getId());
            if (result != null) {
                checkStartOidNode(result, idToken, valueToken);
            } else {
                result = create(null, idToken, valueToken);
            }
        } else {
            result = m_standardSubIdMap.get(valueToken.getValue());
            if (result != null) {
                checkStartOidNode(result, idToken, valueToken);
            } else {
                m_pr.reportInvalidOidStart(valueToken.getValue());
                result = createDummy(null, idToken, valueToken);
            }
        }

        return result;
    }

    private void checkStartOidNode(OidNode result, IdToken idToken, BigIntegerToken valueToken) {
        /* what was I thinking here?
        if (result.getParent() != null) {
            if (idToken != null) {
                m_pr.reportNotAValidStartNode(idToken.getLocation(), idToken.getId());
            } else {
                m_pr.reportNotAValidStartNode(valueToken.getLocation(), valueToken.getValue());
            }
        }
        */
        if (idToken != null && !idToken.getId().equals(result.getId())) {
            // this happens when two OID's are referred to with several names,
            // for example atmMIB/mib_2_37 and atmMIBObjects and mib-2_37_1
            if (!ignoreOidNameMismatch(idToken.getId(), result.getId())) {
                m_pr.reportIdMismatch(idToken.getLocation(), idToken.getId(), result.getId(), result.getLocation(), "checkStartOidNode");
            }
        }
        if (valueToken != null && !valueToken.getValue().equals(result.getValue())) {
            m_pr.reportNumberMismatch(valueToken.getLocation(), valueToken.getValue(), result.getValue());
        }
    }

    // TODO factor out this heuristic stuff into a strategy
    // other alternative: provide support for replacing certain identifiers at the lexer level
    private boolean ignoreOidNameMismatch(String id1, String id2) {
        //return id1.contains("_") || id2.contains("_");
        return false; // we want to be reminded of this all the time.
    }


    public void printUnresolved() {
        for (Map.Entry<String, OidNode> entry : m_idNodeMap.entrySet()) {
            String id = entry.getKey();
            OidNode node = entry.getValue();
            if (!isResolved(node)) {
                m_pr.reportUnresolvedOid(id);
            } else {
                //System.out.println(id + ": " + node);
            }
        }
    }


    public OidNode findNode(String id) {
        return m_idNodeMap.get(id);
    }


    private boolean isResolved(OidNode node) {
        return node.getRoot() == root_;
    }


    public OidNode resolve2(OidNode parent, IdToken idToken, BigIntegerToken valueToken) {
        OidNode result = null;

        if (parent == null && idToken == null) {
            // it must be one of the three roots
        } else if (idToken != null) {
            result = findNode(idToken.getId());
            if (result != null) {

                // TODO parent stuff
            } else if (parent != null) {
                // look by name isn't necessary: it would have been found above
                if (valueToken != null) {
                    result = parent.findChild(valueToken.getValue());
                    if (result != null) {
                        assert(result.m_idToken == null);
                        result.m_idToken = idToken;
                    }
                }
            }
        } else {

        }

        assert(result != null);
        return result;
    }


    public OidNode resolve3(OidNode parent, IdToken idToken, BigIntegerToken valueToken) {
        assert(idToken != null || valueToken != null);
        OidNode result = null;

        if (parent == null) {
            if (idToken != null) {

            } else {
                // one of the three roots
            }
        } else {
            //result = parent.resolveChild()
        }


        assert(result != null);
        return result;
    }

    OidNode create(OidNode parent, IdToken idToken, BigIntegerToken valueToken) {
        OidNode result = new OidNode(this, parent, idToken, valueToken);
        if (idToken != null) {
            assert(m_idNodeMap.get(result.getId()) == null);
            m_idNodeMap.put(result.getId(), result);
        }
        if (parent != null) {
            parent.m_children.add(result);
        }
        return result;
    }

    OidNode createDummy(OidNode parent, IdToken idToken, BigIntegerToken valueToken) {
        return new OidNode(this, parent, idToken, valueToken);
    }

}