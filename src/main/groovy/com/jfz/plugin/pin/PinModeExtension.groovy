package com.jfz.plugin.pin

class PinModeExtension {

    /**
     * 自定义指定包含的目录
     */
    public Set<String> include = new HashSet<>(16)

    /**
     * 自定义匹配模块的模式
     */
    public String pattern

    /**
     * 默认匹配模块
     *
     * m -> module
     * p -> page
     * c -> component
     */
    public String defaultPattern = "^(m_|p_|c_).+?"


    @Override
    public String toString() {
        return "PinModeExtension{" +
                "include=" + include +
                ", pattern='" + pattern + '\'' +
                ", defaultPattern='" + defaultPattern + '\'' +
                '}';
    }

}