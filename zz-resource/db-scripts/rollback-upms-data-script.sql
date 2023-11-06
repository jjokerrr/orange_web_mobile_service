-- ----------------------------
-- 该脚本用于删除自动生成的用户权限管理数据。在搭建初始环境的时候，不要执行该脚本。
-- 再次重新生成项目的时候，如果您在生成器中新增了用户权限相关的数据，而之前已经搭建好的数据库中，也存在了
-- 您自己手动插入的权限数据时，可以通过执行该脚本，将现有数据库表中，生成器生成的权限数据删除，删除之后，可以再执行新生成的数据库脚本数据。
-- 请仅在下面的数据库链接中执行该脚本。
-- 主数据源 [localhost:3306/flow_bupt]
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 全部菜单数据
-- ----------------------------
BEGIN;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707254235137;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623744;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061831;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061832;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061833;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061834;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061835;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061836;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623745;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061845;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061846;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061847;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061848;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623746;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061855;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061856;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061857;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061858;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061859;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061860;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061861;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061862;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061863;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061864;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623747;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061875;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061876;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061877;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061878;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061879;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623748;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061887;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061888;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061889;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061890;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061891;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623749;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061899;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061900;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061901;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061902;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061903;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061904;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061905;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061906;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623750;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061917;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061918;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061919;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061920;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061921;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623751;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061929;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689454707262623752;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061933;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1689458209066061934;

-- ----------------------------
-- 以下记录用于移动端，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1687821642446671872;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1688105082400280576;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1687821728979357696;

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1634009076981567488;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1392786950682841088;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1392786549942259712;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1392786476428693504;

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418057714138877952;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418059005175009280;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418057835631087616;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058049951633408;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058115667988480;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058170542067712;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058289182150656;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058515099947008;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058602723151872;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058744037642240;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058844164067328;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418058907674218496;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1639626065007611904;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418059167532322816;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1418059283920064512;
DELETE FROM `bupt_sys_menu` WHERE menu_id = 1423161217970606080;
COMMIT;

-- ----------------------------
-- 全部权限字数据
-- ----------------------------
BEGIN;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061880;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061885;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061843;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061870;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061892;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061872;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061907;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061910;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061865;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061893;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061935;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061924;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061926;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061839;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061849;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061927;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061894;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061837;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061914;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061895;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061853;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061866;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061913;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061909;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061937;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061930;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061838;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1690569243176734720;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061897;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061884;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061873;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061923;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061840;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061842;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061922;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061883;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061912;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061841;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061881;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061925;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061851;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061908;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061867;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061868;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061852;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061911;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061915;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061869;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061871;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061882;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061896;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061931;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061850;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1690569243185123328;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1689458209066061936;

-- ----------------------------
-- 以下记录用于移动端，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1696168267019718656;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1696168620435968000;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1696168739763916800;

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1639622363244924928;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1639622659975155712;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1400639252747784192;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1400639030462255104;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1400638885750378496;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1400638673195634688;

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418046729906819072;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418048134759583744;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418048872671875072;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418050031436435456;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418057020824621056;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1423636498195943424;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418046848794365952;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418046986677915648;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418047095188754432;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418047182946177024;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418048205177753600;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418050797706416128;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418048335343783936;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418049164754817024;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418049287106859008;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418049398776008704;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1639625791975198720;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418050322282057728;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1418057346877231104;
DELETE FROM `bupt_sys_perm_code` WHERE perm_code_id = 1423637544578322432;
COMMIT;

-- ----------------------------
-- 全部菜单和权限字关系数据
-- ----------------------------
BEGIN;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061831 AND perm_code_id = 1689458209066061838;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061832 AND perm_code_id = 1689458209066061839;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061833 AND perm_code_id = 1689458209066061840;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061834 AND perm_code_id = 1689458209066061841;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061835 AND perm_code_id = 1689458209066061842;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061836 AND perm_code_id = 1689458209066061843;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061845 AND perm_code_id = 1689458209066061850;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061846 AND perm_code_id = 1689458209066061851;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061847 AND perm_code_id = 1689458209066061852;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061848 AND perm_code_id = 1689458209066061853;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061857 AND perm_code_id = 1689458209066061866;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061862 AND perm_code_id = 1689458209066061867;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061858 AND perm_code_id = 1689458209066061868;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061859 AND perm_code_id = 1689458209066061869;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061860 AND perm_code_id = 1689458209066061870;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061863 AND perm_code_id = 1689458209066061872;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061864 AND perm_code_id = 1689458209066061873;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061861 AND perm_code_id = 1689458209066061871;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061875 AND perm_code_id = 1689458209066061881;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061876 AND perm_code_id = 1689458209066061882;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061877 AND perm_code_id = 1689458209066061883;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061878 AND perm_code_id = 1689458209066061884;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061879 AND perm_code_id = 1689458209066061885;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061887 AND perm_code_id = 1689458209066061893;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061888 AND perm_code_id = 1689458209066061894;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061889 AND perm_code_id = 1689458209066061895;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061890 AND perm_code_id = 1689458209066061896;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061891 AND perm_code_id = 1689458209066061897;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061899 AND perm_code_id = 1689458209066061908;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061900 AND perm_code_id = 1689458209066061909;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061901 AND perm_code_id = 1689458209066061910;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061902 AND perm_code_id = 1689458209066061911;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061903 AND perm_code_id = 1689458209066061912;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061904 AND perm_code_id = 1689458209066061913;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061905 AND perm_code_id = 1689458209066061914;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061906 AND perm_code_id = 1689458209066061915;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689454707262623750 AND perm_code_id = 1689458209066061923;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689454707262623750 AND perm_code_id = 1689458209066061924;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689454707262623750 AND perm_code_id = 1689458209066061925;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689454707262623750 AND perm_code_id = 1689458209066061926;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689454707262623750 AND perm_code_id = 1689458209066061927;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061917 AND perm_code_id = 1689458209066061923;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061918 AND perm_code_id = 1689458209066061924;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061919 AND perm_code_id = 1689458209066061925;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061920 AND perm_code_id = 1689458209066061926;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061921 AND perm_code_id = 1689458209066061927;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061929 AND perm_code_id = 1689458209066061931;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061933 AND perm_code_id = 1689458209066061936;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1689458209066061934 AND perm_code_id = 1689458209066061937;

-- ----------------------------
-- 以下记录用于移动端，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1687821728979357696 AND perm_code_id = 1696168620435968000;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1688105082400280576 AND perm_code_id = 1696168739763916800;

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1392786549942259712 AND perm_code_id = 1400638885750378496;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1392786950682841088 AND perm_code_id = 1400639252747784192;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1634009076981567488 AND perm_code_id = 1639622659975155712;

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------

DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418057835631087616 AND perm_code_id = 1418046848794365952;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058049951633408 AND perm_code_id = 1418046986677915648;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058115667988480 AND perm_code_id = 1418047095188754432;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058170542067712 AND perm_code_id = 1418047182946177024;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058289182150656 AND perm_code_id = 1418048205177753600;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058602723151872 AND perm_code_id = 1418048335343783936;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058744037642240 AND perm_code_id = 1418049164754817024;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058844164067328 AND perm_code_id = 1418049287106859008;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058907674218496 AND perm_code_id = 1418049398776008704;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1639626065007611904 AND perm_code_id = 1639625791975198720;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418059167532322816 AND perm_code_id = 1418050322282057728;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418058515099947008 AND perm_code_id = 1418050797706416128;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1418059283920064512 AND perm_code_id = 1418057346877231104;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1423161217970606080 AND perm_code_id = 1423636498195943424;
DELETE FROM `bupt_sys_menu_perm_code` WHERE menu_id = 1423161217970606080 AND perm_code_id = 1423637544578322432;
COMMIT;

-- ----------------------------
-- 全部权限资源模块数据
-- ----------------------------
BEGIN;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689454707271012353;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689454707271012352;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689454707245846530;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867592;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689454707396841472;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689454707350704128;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867549;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867561;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867569;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867577;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867583;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209061867593;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1694976956031832064;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209066061824;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1689458209066061826;

-- ----------------------------
-- 以下记录用于移动端，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1696167234029752320;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1696167285498056704;

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400626463740268544;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400626237478539264;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400626192725315584;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400626018774945792;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400625906245963776;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400625585851469824;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400625338886656000;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400625224646397952;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1413404952566435840;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1400625106979393536;

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418028920103505920;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418038955105849344;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418029566533832704;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418029615040958464;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418030256765276160;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418031999276290048;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418051228918616064;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418051634793025536;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1418054414190514176;
DELETE FROM `bupt_sys_perm_module` WHERE module_id = 1423635643371622400;
COMMIT;

-- ----------------------------
-- 全部权限资源数据
-- ----------------------------
BEGIN;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707417812992;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707417813000;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707417813009;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707417813011;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707417813012;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707430395904;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707430395906;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707430395908;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707430395910;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707363287040;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707363287055;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707363287071;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707363287073;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707363287074;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707380064256;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707380064262;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707380064268;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707380064270;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707392647168;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707296178191;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689454707296178190;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867546;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867547;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867548;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867550;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867551;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867552;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867553;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867554;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867555;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867556;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867557;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867558;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867559;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867560;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867562;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867563;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867564;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867565;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867566;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867567;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867568;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867570;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867571;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867572;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867573;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867574;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867575;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867576;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867578;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867579;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867580;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867581;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867582;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867584;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867585;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867586;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867587;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867588;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867589;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867590;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867591;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867594;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867595;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867596;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867597;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867598;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867599;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867600;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867601;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209061867602;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1694978394602606644;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1694978394602606645;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1694978394602606646;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1694978394602606647;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209066061825;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209066061827;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1689458209066061828;

-- ----------------------------
-- 以下记录用于移动端，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167456139120640;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167558614355968;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167638708785152;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167688499367936;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167785131937792;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167901570011136;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1696167964069335040;

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637970863624193;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637970863624192;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637895328403456;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637858414333952;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637822737584128;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637775962705920;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400637740705386496;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636656679129088;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636610403373056;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636571165659136;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636535371468800;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636501900922880;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636338838966272;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636301480300544;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636258107002880;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636217728438272;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636182936686592;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400636031752998912;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635963469729792;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635896461529088;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635827347787776;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635757332271104;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635661106548736;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635581532213248;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635527698321408;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635426808532992;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635378506928128;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635341274091520;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635267726970880;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400635131969933312;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634735549485056;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634693472227328;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634651944423424;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634613151305728;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634559896227840;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634354593435648;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634280308117504;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634100498305024;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400634012745076736;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633922085195776;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633798403559424;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633713376628736;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633612201627648;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633568027217920;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633529678696448;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633437093629952;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400633375525441536;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400632511620452352;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400632438635368448;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400632326538399744;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400632216169484288;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400632108514283520;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400627109692444672;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400626963806162944;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1400626748101496832;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1639621697092980736;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1639621809328361472;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1639621889154355200;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1639621941444743168;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1413405352866615296;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1413405313788284928;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1413405224021790720;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1413405166471745536;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1413405061278601216;

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418030338508066816;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418030386436378624;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418030437971791872;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418030477918343168;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418030533757112320;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031006660694016;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031078391681024;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031120481521664;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031174604820480;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031219706171392;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031304779239424;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031515207471104;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031648351457280;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031712788549632;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418031801271586816;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418032222232907776;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418032258429751296;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418032300414734336;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418032344064856064;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418032382409183232;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418033804987076608;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418033889183535104;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418034243434450944;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418035997727264768;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418040510253109248;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418040574245605376;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418040574245605377;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1639625448801439744;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418051334564745215;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418051334564745216;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418051485324808192;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418051693639110656;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418054128097038336;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418054618176294912;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418054792965525504;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418055060981551104;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418056212146032640;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418056212146032641;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418079603167072256;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1418079670594703360;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1423635764486344704;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1423635851312631808;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1423636091033882624;
DELETE FROM `bupt_sys_perm` WHERE perm_id = 1423984506028691456;
COMMIT;

-- ----------------------------
-- 全部权限字和权限资源关系数据
-- ----------------------------
BEGIN;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061838 AND perm_id = 1689454707380064256;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061838 AND perm_id = 1689454707380064262;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061839 AND perm_id = 1689454707363287040;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061839 AND perm_id = 1689454707430395904;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061839 AND perm_id = 1689458209061867553;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061840 AND perm_id = 1689454707380064268;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061840 AND perm_id = 1689454707363287055;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061840 AND perm_id = 1689454707380064270;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061840 AND perm_id = 1689454707430395904;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061840 AND perm_id = 1689458209061867553;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061841 AND perm_id = 1689454707363287071;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061842 AND perm_id = 1689454707392647168;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061843 AND perm_id = 1689458209061867548;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061843 AND perm_id = 1689458209061867547;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061843 AND perm_id = 1689458209061867546;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061850 AND perm_id = 1689454707430395904;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061850 AND perm_id = 1689454707430395906;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061851 AND perm_id = 1689454707417812992;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061852 AND perm_id = 1689454707430395908;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061852 AND perm_id = 1689454707417813000;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061852 AND perm_id = 1689454707430395910;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061853 AND perm_id = 1689454707417813009;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061866 AND perm_id = 1689458209061867553;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061868 AND perm_id = 1689458209061867550;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061868 AND perm_id = 1689458209061867565;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061869 AND perm_id = 1689458209061867554;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061869 AND perm_id = 1689458209061867551;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061869 AND perm_id = 1689458209061867565;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061871 AND perm_id = 1689458209061867560;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061871 AND perm_id = 1689458209061867559;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061870 AND perm_id = 1689458209061867552;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061867 AND perm_id = 1689458209061867557;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061872 AND perm_id = 1689458209061867555;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061872 AND perm_id = 1689458209061867558;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061873 AND perm_id = 1689458209061867556;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061881 AND perm_id = 1689458209061867565;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061882 AND perm_id = 1689458209061867562;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061882 AND perm_id = 1689458209061867573;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061883 AND perm_id = 1689458209061867566;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061883 AND perm_id = 1689458209061867564;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061884 AND perm_id = 1689458209061867563;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061883 AND perm_id = 1689458209061867573;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061885 AND perm_id = 1689458209061867567;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061885 AND perm_id = 1689458209061867568;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061893 AND perm_id = 1689458209061867573;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061894 AND perm_id = 1689458209061867570;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061894 AND perm_id = 1689458209061867582;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061895 AND perm_id = 1689458209061867574;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061895 AND perm_id = 1689458209061867571;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061895 AND perm_id = 1689458209061867582;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061896 AND perm_id = 1689458209061867572;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061897 AND perm_id = 1689458209061867575;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061897 AND perm_id = 1689458209061867576;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061908 AND perm_id = 1689458209061867581;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061908 AND perm_id = 1689458209061867582;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061908 AND perm_id = 1689458209061867587;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061909 AND perm_id = 1689458209061867578;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061910 AND perm_id = 1689458209061867579;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061911 AND perm_id = 1689458209061867580;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061912 AND perm_id = 1689458209061867584;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061913 AND perm_id = 1689458209061867588;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061913 AND perm_id = 1689458209061867585;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061914 AND perm_id = 1689458209061867586;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061915 AND perm_id = 1689458209061867589;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061915 AND perm_id = 1689458209061867590;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061915 AND perm_id = 1689458209061867591;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867594;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867595;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867596;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867597;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867598;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867599;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867600;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867601;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1689458209061867602;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1694978394602606644;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1694978394602606645;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1694978394602606646;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061923 AND perm_id = 1694978394602606647;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061931 AND perm_id = 1689458209066061825;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061936 AND perm_id = 1689458209066061827;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1689458209066061937 AND perm_id = 1689458209066061828;

-- ----------------------------
-- 以下记录用于移动端，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167456139120640;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167456139120640;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167558614355968;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167558614355968;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167638708785152;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167638708785152;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167688499367936;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167688499367936;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167785131937792;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167785131937792;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167901570011136;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167901570011136;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168620435968000 AND perm_id = 1696167964069335040;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1696168739763916800 AND perm_id = 1696167964069335040;

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1639622659975155712 AND perm_id = 1400626748101496832;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1639622659975155712 AND perm_id = 1639621697092980736;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1639622659975155712 AND perm_id = 1639621809328361472;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1639622659975155712 AND perm_id = 1639621889154355200;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1639622659975155712 AND perm_id = 1639621941444743168;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400626748101496832;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400626963806162944;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400627109692444672;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400632108514283520;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400632216169484288;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400632326538399744;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400632438635368448;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400638885750378496 AND perm_id = 1400632511620452352;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400626748101496832;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400626963806162944;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400627109692444672;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400632108514283520;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400632216169484288;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633375525441536;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633437093629952;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633529678696448;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633568027217920;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633612201627648;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633713376628736;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633798403559424;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400633922085195776;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634012745076736;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634100498305024;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634280308117504;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634354593435648;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634559896227840;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634613151305728;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634651944423424;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634693472227328;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400634735549485056;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635131969933312;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635267726970880;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635341274091520;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635378506928128;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635426808532992;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635527698321408;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635581532213248;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635661106548736;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635757332271104;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635827347787776;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635896461529088;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400635963469729792;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636031752998912;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636182936686592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636217728438272;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636258107002880;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636301480300544;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636338838966272;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636501900922880;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636535371468800;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636571165659136;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636610403373056;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400636656679129088;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637740705386496;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637775962705920;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637822737584128;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637858414333952;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637895328403456;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637970863624193;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1400637970863624192;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1413405352866615296;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1413405313788284928;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1413405224021790720;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1413405166471745536;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1400639252747784192 AND perm_id = 1413405061278601216;

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418051334564745215;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1400637970863624192;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1400637970863624192;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1400637970863624192;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1400633375525441536;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1400635131969933312;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1400635661106548736;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1400636501900922880;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1400637740705386496;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1413405061278601216;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418026612762349580;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1400637775962705920;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1400637970863624192;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418026612762349572;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418026612762349572;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418046848794365952 AND perm_id = 1418030338508066816;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418046986677915648 AND perm_id = 1418030386436378624;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418047095188754432 AND perm_id = 1418030437971791872;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418047182946177024 AND perm_id = 1418030477918343168;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418047095188754432 AND perm_id = 1418030533757112320;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048205177753600 AND perm_id = 1418031006660694016;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031078391681024;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031120481521664;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031174604820480;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031219706171392;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031304779239424;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031515207471104;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031648351457280;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031712788549632;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418031801271586816;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418032222232907776;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418032258429751296;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418032300414734336;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418032344064856064;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418032382409183232;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049164754817024 AND perm_id = 1418033804987076608;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418033889183535104;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049164754817024 AND perm_id = 1418034243434450944;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418034243434450944;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049164754817024 AND perm_id = 1418035997727264768;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418035997727264768;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049287106859008 AND perm_id = 1418040510253109248;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049287106859008 AND perm_id = 1418040574245605377;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049398776008704 AND perm_id = 1418040574245605376;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1639625791975198720 AND perm_id = 1639625448801439744;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418051334564745216;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418051485324808192;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418051693639110656;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418054128097038336;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049164754817024 AND perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418054618176294912;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418054618176294912;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418049164754817024 AND perm_id = 1418054792965525504;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418054792965525504;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418054792965525504;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418055060981551104;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418056212146032640;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418079603167072256;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418079603167072256;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050322282057728 AND perm_id = 1418079670594703360;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418050797706416128 AND perm_id = 1418079670594703360;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418057346877231104 AND perm_id = 1418079670594703360;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1418048335343783936 AND perm_id = 1689454707380064256;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1418034243434450944;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1418054519798894592;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1418054618176294912;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1418054792965525504;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1423635764486344704;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1423635851312631808;
DELETE FROM `bupt_sys_perm_code_perm` WHERE perm_code_id = 1423637544578322432 AND perm_id = 1423636091033882624;

DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/login/getLoginInfo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysRole/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysRole/listDictByIds';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysUser/list';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysMenu/listMenuDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysDept/listAllChildDeptIdByParentIds';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/globalDict/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/globalDict/listDictByIds';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/globalDict/listAll';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysDept/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/sysDept/listDictByIds';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/app/areaCode/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/app/areaCode/listAll';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/app/areaCode/listDictByIds';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/app/areaCode/listDictByParentId';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/login/doLogout';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/login/changePassword';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/login/changeHeadImage';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/upms/login/downloadHeadImage';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/mobile/mobileEntry/downloadImage';

-- ----------------------------
-- 以下记录用于在线表单，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/online/onlineDblink/testConnection';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/online/onlineOperation/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/online/onlineOperation/getColumnRuleCode';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/online/onlineDict/listAllGlobalDict';

-- ----------------------------
-- 以下记录用于工作流，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowCategory/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowEntry/listDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowEntry/viewDict';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOnlineOperation/listFlowEntryForm';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOnlineOperation/viewCopyBusinessData';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOnlineOperation/viewDraftData';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewDraftData';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/countRuntimeTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewInitialTaskInfo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewRuntimeTaskInfo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewHistoricTaskInfo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewInitialHistoricTaskInfo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewTaskUserInfo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/submitConsign';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/listMultiSignAssignees';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/listFlowTaskComment';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewProcessBpmn';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewHighlightFlowData';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/cancelWorkOrder';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/remindRuntimeTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/listRejectCandidateUserTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/rejectToStartUserTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/rejectRuntimeTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/revokeHistoricTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/freeJumpTo';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowOperation/viewCopyBusinessData';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowMessage/getMessageCount';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowMessage/listRemindingTask';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/flow/flowMessage/listCopyMessage';

-- ----------------------------
-- 以下记录用于通用扩展，这里的注释，是为了便于老用户进行手动数据补偿。
-- ----------------------------
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/commonext/bizwidget/list';
DELETE FROM `bupt_sys_perm_whitelist` WHERE perm_url = '/admin/commonext/bizwidget/view';

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
