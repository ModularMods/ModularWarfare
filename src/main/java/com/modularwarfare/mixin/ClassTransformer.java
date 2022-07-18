package com.modularwarfare.mixin;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (transformedName.equals("net.minecraft.client.Minecraft") || transformedName.equals("bib")) {

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            for (MethodNode method : cn.methods) {

                if (!(method.name.equals("processKeyBinds") || method.name.equals("func_184117_aA") || method.name.equals("aE"))) continue;

                for (AbstractInsnNode ain : method.instructions.toArray()) {
                    if (!(ain.getOpcode() == INVOKEVIRTUAL)) continue;

                    MethodInsnNode min = (MethodInsnNode) ain;

                    if (min.name.equals("onHotbarSelected") || min.name.equals("func_175260_a") || min.name.equals("a")) {

                        LabelNode jumpLabel = null;

                        for (int i = 1;; i++) {

                            AbstractInsnNode insnNode = method.instructions.get(method.instructions.indexOf(min) + i);

                            if (insnNode.getOpcode() == GOTO) {
                                jumpLabel = ((JumpInsnNode) insnNode).label;
                            }

                            if(jumpLabel != null && insnNode.getOpcode() == ALOAD) {
                                InsnList injectInsnList = new InsnList();
                                injectInsnList.add(new MethodInsnNode(INVOKESTATIC, "com/modularwarfare/InjectMethods", "isReloading", "()Z", false));
                                injectInsnList.add(new JumpInsnNode(IFNE, jumpLabel));
                                method.instructions.insertBefore(insnNode, injectInsnList);
                                break;
                            }

                        }

                    }

                }

            }

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();

        }

        return basicClass;
    }

}
