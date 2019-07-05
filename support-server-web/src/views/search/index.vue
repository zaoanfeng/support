<template>
  <div>
    <div class="navbar">
      <div v-if="!showSearch" class="left-menu">
        <div>
          <img src="@/assets/logo/logo_blue.png" height="40px" style="margin-right:20px;">
        </div>
        <el-input v-model="keyword" placeholder="请输入搜索内容" style="width:500px">
          <el-button slot="append" icon="el-icon-search" @click="onSubmit"/>
        </el-input>
      </div>
      <div class="right-menu">
        <template v-if="device!=='mobile'">
          <!-- <search class="right-menu-item" /> -->

          <error-log class="errLog-container right-menu-item hover-effect"/>

          <screenfull class="right-menu-item hover-effect"/>

          <el-tooltip :content="$t('navbar.size')" effect="dark" placement="bottom">
            <size-select class="right-menu-item hover-effect"/>
          </el-tooltip>

          <lang-select class="right-menu-item hover-effect"/>
        </template>

        <div v-if="islogin" class="right-menu">
          <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
            <div class="avatar-wrapper">
              <img :src="avatar+'?imageView2/1/w/80/h/80'" class="user-avatar">
              <i class="el-icon-caret-bottom"/>
            </div>
            <el-dropdown-menu slot="dropdown">
              <router-link to="/">
                <el-dropdown-item>{{ $t('navbar.dashboard') }}</el-dropdown-item>
              </router-link>
              <a target="_blank" @click="manager()">
                <el-dropdown-item>{{ $t('navbar.github') }}</el-dropdown-item>
              </a>
              <el-dropdown-item divided>
                <span style="display:block;" @click="logout">{{ $t('navbar.logOut') }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div v-else class="right-menu-item">
          <a
            target="_blank"
            class="link-type"
            style="font-family: cursive;"
            @click="showDialog = true"
          >登录</a>
        </div>
      </div>
      <el-dialog :title="$t('login.title')" :visible.sync="showDialog">
        <login @getAction="callShowDialog" />
      </el-dialog>
    </div>
    <div class="main">
      <div v-if="showSearch" style="margin: 0 auto; margin-top: 200px; width:40%; text-align:center">
        <img src="@/assets/logo/logo_blue.png" style="margin-bottom:30px">
        <el-input v-model="keyword" placeholder="请输入搜索内容" @keydown.enter.native="onSubmit">
          <el-button slot="append" icon="el-icon-search" @click="onSubmit"/>
        </el-input>
      </div>
      <div v-else style="margin: 0 auto; width:60%; test-align:center;">
        <el-table
          v-loading="loading"
          :data="tableData"
          element-loading-text="拼命加载中"
          style="width: 100%">
          <el-table-column >
            <template slot-scope="scope" >
              <router-link :to="{path:'search/detail', query:{type:scope.row.type, id:scope.row.id}}" tag="a" target="_blank" v-html="scope.row.content">{{ scope.row.content }}</router-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="bottom">版权声明：网站所有图片及内容版权归浙江汉朔电子科技有限公司所有 浙ICP备 14040138号-2</div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import ErrorLog from '@/components/ErrorLog'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import LangSelect from '@/components/LangSelect'
import Login from '../login'
// import { validUsername } from '@/utils/validate'
import { search } from '@/api/remoteSearch'
import { getToken } from '@/utils/auth'

export default {
  name: 'Search',
  components: {
    ErrorLog,
    Screenfull,
    SizeSelect,
    LangSelect,
    Login
  },
  data() {
    return {
      tableData: [{}],
      loading: false,
      showDialog: false,
      redirect: undefined,
      keyword: '',
      islogin: false,
      showSearch: true
    }
  },
  computed: {
    ...mapGetters(['sidebar', 'name', 'avatar', 'device'])
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    // window.addEventListener('hashchange', this.afterQRScan)
    if (getToken() === undefined || getToken() === '') {
      this.islogin = false
    } else {
      this.islogin = true
    }
  },
  destroyed() {
    // window.removeEventListener('hashchange', this.afterQRScan)
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('toggleSideBar')
    },
    manager() {
      this.$router.push({ path: '/dashboard' })
    },
    token() {
      console.log(getToken())
      return getToken()
    },
    callShowDialog(showDialog) {
      if (getToken()) {
        this.showDialog = false
        this.islogin = true
      }
    },
    logout() {
      this.$store.dispatch('FedLogOut').then(() => {
        location.reload() // In order to re-instantiate the vue-router object to avoid bugs
      })
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
    },
    onSubmit() {
      if (this.keyword === '') {
        this.showSearch = true
      } else {
        this.showSearch = false
        this.loading = true
        search(this.keyword, 1, 10).then(response => {
          this.tableData = response.data
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
/* 修复input 背景不协调 和光标变色 */
/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */

$bg: #fdfdfd;
$light_gray: #eee;
$cursor: #fff;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
    &::first-line {
      color: $light_gray;
    }
  }
}

/* reset element-ui css */
.bottom {
  bottom: 0px;
  position: fixed;
  text-align: center;
  width: 100%;
  line-height: 60px;
  font-family: cursive;
  font-size: larger;
  font-style: oblique;
  color: darkgray;
}
</style>

<style rel="stylesheet/scss" lang="scss" scoped>
$bg: #ffffff;
$dark_gray: #889aa4;
$light_gray: #eee;

.main {
  margin-bottom: 60px;
  z-index: 1;
  position: relative;
}

.bottom {
  bottom: 0px;
  position: fixed;
  text-align: center;
  width: 100%;
  line-height: 60px;
  font-family: cursive;
  font-size: smaller;
  font-style: oblique;
  color: darkgray;
}

.navbar {
  height: 50px;
  overflow: hidden;
  border-bottom: 1px solid ghostwhite;

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .left-menu {
    height: 46px;
    line-height: 46px;
    float: left;
    padding-left: 20px;
    display: -webkit-inline-box;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
